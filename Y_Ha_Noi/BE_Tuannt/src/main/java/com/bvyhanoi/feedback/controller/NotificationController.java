package com.bvyhanoi.feedback.controller;

import com.bvyhanoi.feedback.dto.NotificationDTO;
import com.bvyhanoi.feedback.service.AuthService;
import com.bvyhanoi.feedback.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationDTO>> getNotifications() {
        Long userId = authService.getCurrentUser().getId();
        List<NotificationDTO> notifications = notificationService.getNotifications(userId);
        return ResponseEntity.ok(notifications);
    }
    
    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        Long userId = authService.getCurrentUser().getId();
        long count = notificationService.getUnreadCount(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> markAllAsRead() {
        Long userId = authService.getCurrentUser().getId();
        notificationService.markAllAsRead(userId);
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long id) {
        Long userId = authService.getCurrentUser().getId();
        notificationService.markAsRead(id, userId);
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        return ResponseEntity.ok(response);
    }
}

