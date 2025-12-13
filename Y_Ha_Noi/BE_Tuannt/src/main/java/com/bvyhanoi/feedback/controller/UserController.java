package com.bvyhanoi.feedback.controller;

import com.bvyhanoi.feedback.dto.CreateUserRequest;
import com.bvyhanoi.feedback.dto.PagedResponse;
import com.bvyhanoi.feedback.dto.UpdateUserRequest;
import com.bvyhanoi.feedback.dto.UserDTO;
import com.bvyhanoi.feedback.entity.User;
import com.bvyhanoi.feedback.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedResponse<UserDTO>> getUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) User.Role role,
            @RequestParam(required = false) User.UserStatus status) {
        PagedResponse<UserDTO> response = userService.getAllUsers(page, size, keyword, role, status);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDTO user = userService.createUser(request);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        UserDTO user = userService.updateUser(id, request);
        return ResponseEntity.ok(user);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> resetPassword(@PathVariable Long id) {
        String newPassword = userService.resetPassword(id);
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        response.put("newPassword", newPassword);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> toggleStatus(@PathVariable Long id) {
        UserDTO user = userService.toggleStatus(id);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/handlers")
    public ResponseEntity<List<UserDTO>> getHandlers(@RequestParam(required = false) Long departmentId) {
        List<UserDTO> handlers = userService.getHandlers(departmentId);
        return ResponseEntity.ok(handlers);
    }
}

