package ovi.web.dhybe.service;


import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ovi.web.dhybe.dto.auth.ChangePasswordRequest;
import ovi.web.dhybe.dto.auth.UserDto;
import ovi.web.dhybe.repository.UserDtoRepository;
import ovi.web.dhybe.utils.JwtTokenUtil;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDtoRepository userDtoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Map<String, Object> login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenUtil.generateToken(
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal());

        // Load full user info (excluding password) so FE can store it
        UserDto user = userDtoRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        userInfo.put("departmentId", user.getDepartmentId());
        userInfo.put("status", user.getStatus());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("user", userInfo);
        return response;
    }

    public void changePassword(String username, ChangePasswordRequest request) {
        UserDto user = userDtoRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password confirmation does not match");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userDtoRepository.save(user);
    }

    // ✅ Hàm kiểm tra và lấy thông tin từ token
    public Map<String, Object> getUserInfoFromToken(String token) {
        Map<String, Object> info = new HashMap<>();

        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            Claims claims = jwtTokenUtil.extractAllClaims(token);

            info.put("username", claims.getSubject());
            info.put("issuedAt", claims.getIssuedAt());
            info.put("claims", claims);

        } catch (Exception e) {
            info.put("error", "Token không hợp lệ hoặc đã hết hạn: " + e.getMessage());
        }

        return info;
    }
}
