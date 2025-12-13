package com.bvyhanoi.feedback.dto;

import com.bvyhanoi.feedback.entity.Notification;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {
    private Long id;
    private Long userId;
    private Notification.NotificationType type;
    private String title;
    private String message;
    private Boolean read;
    private LocalDateTime createdAt;
    private Long feedbackId;
    private Long ratingId;
}

