package ovi.web.dhybe.dto.feedback;

import lombok.Builder;
import lombok.Data;
import ovi.web.dhybe.enums.FeedbackLevel;
import ovi.web.dhybe.enums.FeedbackStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackListItemResponse {
    private Long id;
    private String code;
    private FeedbackStatus status;
    private FeedbackLevel level;
    private String departmentName;
    private String doctorName;
    private LocalDateTime receivedDate;
    private LocalDateTime completedDate;
    private Long handlerId;
    private String handlerName;
}





