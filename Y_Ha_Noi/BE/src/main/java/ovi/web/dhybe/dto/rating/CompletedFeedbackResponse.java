package ovi.web.dhybe.dto.rating;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CompletedFeedbackResponse {
    private Long id;
    private String code;
    private String content;
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private LocalDateTime completedDate;
    private Integer rating;
    private String comment;
}





