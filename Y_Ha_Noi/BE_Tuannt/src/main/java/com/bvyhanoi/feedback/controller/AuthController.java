package com.bvyhanoi.feedback.controller;

import com.bvyhanoi.feedback.dto.AuthResponse;
import com.bvyhanoi.feedback.dto.ChangePasswordRequest;
import com.bvyhanoi.feedback.dto.ForgotPasswordRequest;
import com.bvyhanoi.feedback.dto.LoginRequest;
import com.bvyhanoi.feedback.dto.RefreshTokenRequest;
import com.bvyhanoi.feedback.dto.UpdateProfileRequest;
import com.bvyhanoi.feedback.entity.User;
import com.bvyhanoi.feedback.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        User user = authService.getCurrentUser();
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify() {
        Map<String, Object> response = new HashMap<>();
        try {
            authService.getCurrentUser();
            response.put("valid", true);
        } catch (Exception e) {
            response.put("valid", false);
        }
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        response.put("message", "Password reset link has been sent to your email");
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = authService.updateProfile(request);
        return ResponseEntity.ok(user);
    }
}
