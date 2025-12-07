package com.bvyhanoi.feedback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "feedback_id", nullable = false)
    private Long feedbackId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Feedback.FeedbackStatus status;
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content; // Nội dung xử lý
    
    @Column(columnDefinition = "TEXT")
    private String note; // Ghi chú thêm
    
    @Column(name = "image_ids", columnDefinition = "TEXT")
    private String imageIds; // JSON array of image IDs, e.g., "[1,2,3]"
    
    @Column(name = "created_by", nullable = false)
    private Long createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

