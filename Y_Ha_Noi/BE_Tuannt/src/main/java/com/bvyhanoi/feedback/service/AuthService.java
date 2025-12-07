package com.bvyhanoi.feedback.service;

import com.bvyhanoi.feedback.dto.AuthResponse;
import com.bvyhanoi.feedback.dto.ChangePasswordRequest;
import com.bvyhanoi.feedback.dto.ForgotPasswordRequest;
import com.bvyhanoi.feedback.dto.LoginRequest;
import com.bvyhanoi.feedback.entity.User;
import com.bvyhanoi.feedback.repository.UserRepository;
import com.bvyhanoi.feedback.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new RuntimeException("Invalid username or password"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("User account is inactive");
        }
        
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getUsername());
        
        return new AuthResponse(accessToken, refreshToken, user);
    }
    
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        
        String username = jwtUtil.extractUsername(refreshToken);
        Long userId = jwtUtil.extractUserId(refreshToken);
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new RuntimeException("User account is inactive");
        }
        
        String newAccessToken = jwtUtil.generateAccessToken(userId, username, user.getRole().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, username);
        
        return new AuthResponse(newAccessToken, newRefreshToken, user);
    }
    
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();
        
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }
        
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("Email not found"));
        
        // TODO: Send email with reset link
        // For now, just log it
        System.out.println("Password reset requested for: " + request.getEmail());
    }
    
    @Transactional
    public User updateProfile(com.bvyhanoi.feedback.dto.UpdateProfileRequest request) {
        User user = getCurrentUser();
        
        // Check if email is being changed and if it already exists
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
        }
        
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        
        return userRepository.save(user);
    }
}

