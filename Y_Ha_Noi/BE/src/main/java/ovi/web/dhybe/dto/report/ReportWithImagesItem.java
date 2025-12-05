package ovi.web.dhybe.dto.report;

import lombok.Builder;
import lombok.Data;
import ovi.web.dhybe.dto.feedback.FeedbackImageResponse;

import java.util.List;

@Data
@Builder
public class ReportWithImagesItem {
    private Long feedbackId;
    private String code;
    private String content;
    private String departmentName;
    private String status;
    private List<FeedbackImageResponse> feedbackImages;
    private List<FeedbackImageResponse> processImages;
}





