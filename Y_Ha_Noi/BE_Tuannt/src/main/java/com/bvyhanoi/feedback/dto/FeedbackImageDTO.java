package com.bvyhanoi.feedback.dto;

import lombok.Data;

@Data
public class FeedbackImageDTO {
    private Long id;
    private String filename;
    private String url;
    private String imageType;
}

