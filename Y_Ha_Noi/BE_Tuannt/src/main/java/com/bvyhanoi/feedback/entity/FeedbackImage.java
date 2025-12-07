package com.bvyhanoi.feedback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackImage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "feedback_id", nullable = false)
    private Long feedbackId;
    
    @Column(nullable = false, length = 255)
    private String filename;
    
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 20)
    private ImageType imageType;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum ImageType {
        FEEDBACK, PROCESS
    }
}

