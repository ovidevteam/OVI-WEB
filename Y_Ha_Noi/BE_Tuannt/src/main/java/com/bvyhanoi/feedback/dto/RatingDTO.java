package com.bvyhanoi.feedback.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RatingDTO {
    private Long id;
    private Long feedbackId;
    private Long userId; // User who created this rating
    private String userName; // User name who created this rating
    private Long doctorId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

