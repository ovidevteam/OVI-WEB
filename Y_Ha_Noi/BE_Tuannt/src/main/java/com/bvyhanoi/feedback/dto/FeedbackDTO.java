package com.bvyhanoi.feedback.dto;

import com.bvyhanoi.feedback.entity.Feedback;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FeedbackDTO {
    private Long id;
    private String code;
    private String content;
    private Feedback.FeedbackChannel channel;
    private Feedback.FeedbackLevel level;
    private Feedback.FeedbackStatus status;
    private Long departmentId;
    private String departmentName;
    private Long doctorId;
    private Long handlerId;
    private String handlerName;
    private Long receiverId;
    private String receiverName;
    private LocalDate receivedDate;
    private LocalDate completedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FeedbackImageDTO> images;
    // Rating fields for completed feedbacks
    private Integer rating; // Current user's rating
    private String comment; // Current user's comment
    private Double averageRating; // Average rating from all users
    private Boolean userHasRated; // Whether current user has rated
    private String doctorName;
}

