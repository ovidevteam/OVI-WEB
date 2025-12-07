package com.bvyhanoi.feedback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationType type;
    
    @Column(nullable = false, length = 200)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(nullable = false)
    private Boolean read = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "feedback_id")
    private Long feedbackId;
    
    @Column(name = "rating_id")
    private Long ratingId;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum NotificationType {
        FEEDBACK,      // Phản ánh mới được tạo
        ASSIGNED,      // Được phân công xử lý
        COMPLETED,     // Phản ánh đã hoàn thành
        RATING         // Có đánh giá mới
    }
}

