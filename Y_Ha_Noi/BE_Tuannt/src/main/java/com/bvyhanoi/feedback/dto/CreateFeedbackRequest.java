package com.bvyhanoi.feedback.dto;

import com.bvyhanoi.feedback.entity.Feedback;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateFeedbackRequest {
    @NotBlank(message = "Content is required")
    private String content;
    
    @NotNull(message = "Channel is required")
    private Feedback.FeedbackChannel channel;
    
    @NotNull(message = "Level is required")
    private Feedback.FeedbackLevel level;
    
    private Long departmentId;
    private Long doctorId;
    private List<Long> imageIds;
}

