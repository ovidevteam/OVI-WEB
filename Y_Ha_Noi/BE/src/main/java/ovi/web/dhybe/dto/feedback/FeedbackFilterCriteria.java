package ovi.web.dhybe.dto.feedback;

import lombok.Data;
import ovi.web.dhybe.enums.FeedbackLevel;
import ovi.web.dhybe.enums.FeedbackStatus;

import java.time.LocalDateTime;

@Data
public class FeedbackFilterCriteria {
    private Long departmentId;
    private Long doctorId;
    private FeedbackStatus status;
    private FeedbackLevel level;
    private Long handlerId;
    private Long receiverId;
    private Boolean ratedOnly;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private String keyword;
}





