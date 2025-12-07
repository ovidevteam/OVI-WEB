package com.bvyhanoi.feedback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "feedbacks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackChannel channel;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackLevel level;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackStatus status = FeedbackStatus.NEW;
    
    @Column(name = "department_id")
    private Long departmentId;
    
    @Column(name = "doctor_id")
    private Long doctorId;
    
    @Column(name = "handler_id")
    private Long handlerId;
    
    @Column(name = "receiver_id")
    private Long receiverId;
    
    @Column(name = "received_date", nullable = false)
    private LocalDate receivedDate;
    
    @Column(name = "completed_date")
    private LocalDate completedDate;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (receivedDate == null) {
            receivedDate = LocalDate.now();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum FeedbackChannel {
        PHONE, EMAIL, WEBSITE, DIRECT
    }
    
    public enum FeedbackLevel {
        LOW, MEDIUM, HIGH
    }
    
    public enum FeedbackStatus {
        NEW, ASSIGNED, PROCESSING, COMPLETED, CANCELLED
    }
}

