package com.bvyhanoi.feedback.dto;

import com.bvyhanoi.feedback.entity.Feedback;
import lombok.Data;

import java.util.List;

@Data
public class ProcessFeedbackRequest {
    private Feedback.FeedbackStatus status;
    private String content; // Nội dung xử lý (required)
    private String note; // Ghi chú (optional)
    private String result; // Deprecated, use content instead
    private List<Long> imageIds;
}

