package com.bvyhanoi.feedback.dto;

import com.bvyhanoi.feedback.entity.Feedback;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackHistoryDTO {
    private Long id;
    private Long feedbackId;
    private Feedback.FeedbackStatus status;
    private String content; // Nội dung xử lý
    private String note; // Ghi chú thêm
    private Long createdBy;
    private String createdByName; // User full name
    private LocalDateTime createdAt;
    private List<Long> imageIds; // IDs of attached images
    private List<FeedbackImageDTO> images; // Image details (optional, for display)
}

