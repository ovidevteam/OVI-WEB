package ovi.web.dhybe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ovi.web.dhybe.dto.auth.ChangePasswordRequest;
import ovi.web.dhybe.dto.auth.UserDto;
import ovi.web.dhybe.dto.common.ApiResponse;
import ovi.web.dhybe.dto.user.HandlerResponse;
import ovi.web.dhybe.entity.Department;
import ovi.web.dhybe.repository.UserDtoRepository;
import ovi.web.dhybe.service.AuthService;
import ovi.web.dhybe.service.DepartmentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserDtoController {

    private final UserDtoRepository userDtoRepository;
    private final DepartmentService departmentService;
    private final AuthService authService;

    @PostMapping("/public/login")
    public ResponseEntity<?> loginPublic(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        return ResponseEntity.ok(authService.login(username, password));
    }

    // Endpoint used by FE: POST /api/auth/login
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        return ResponseEntity.ok(authService.login(username, password));
    }

    @PostMapping("/public/register")
    public ResponseEntity<?> register(@RequestBody UserDto userDto) {
        if (userDtoRepository.findByUsername(userDto.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }
        userDtoRepository.save(userDto);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/public/delete")
    public ResponseEntity<String> deleteUser(@RequestParam int id) {
        if (userDtoRepository.existsById(id)) {
            userDtoRepository.deleteById(id);
        }
        return ResponseEntity.ok("đã xóa:" + id);
    }

    @GetMapping("/token")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(authService.getUserInfoFromToken(token));
    }

    @PutMapping("/auth/change-password")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                            Authentication authentication) {
        authService.changePassword(authentication.getName(), request);
        return ApiResponse.ok("Password updated successfully");
    }

    // Endpoint used by FE: GET /api/auth/me
    @GetMapping("/auth/me")
    @PreAuthorize("isAuthenticated()")
    public Map<String, Object> currentUser(Authentication authentication) {
        UserDto user = userDtoRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("fullName", user.getFullName());
        userInfo.put("email", user.getEmail());
        userInfo.put("role", user.getRole());
        userInfo.put("departmentId", user.getDepartmentId());
        userInfo.put("status", user.getStatus());
        return userInfo;
    }

    // Endpoint used by FE: POST /api/auth/logout (no-op, token is client-side)
    @PostMapping("/auth/logout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok("Logged out");
    }

    @GetMapping("/users/handlers")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER','RECEIVER','HANDLER')")
    public List<HandlerResponse> handlers(@RequestParam(required = false) Long departmentId) {
        return userDtoRepository.findByRoleIgnoreCase("HANDLER").stream()
                .filter(user -> user.getStatus() == null || "ACTIVE".equalsIgnoreCase(user.getStatus()))
                .filter(user -> departmentId == null ||
                        (user.getDepartmentId() != null && user.getDepartmentId().equals(departmentId)))
                .map(this::toHandlerResponse)
                .collect(Collectors.toList());
    }

    private HandlerResponse toHandlerResponse(UserDto user) {
        Department department = null;
        if (user.getDepartmentId() != null) {
            department = departmentService.getEntity(user.getDepartmentId());
        }
        return HandlerResponse.builder()
                .id((long) user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .departmentId(department != null ? department.getId() : null)
                .departmentName(department != null ? department.getName() : null)
                .role(user.getRole())
                .build();
    }
}
