package ovi.web.dhybe.dto.feedback;

import lombok.Builder;
import lombok.Data;
import ovi.web.dhybe.enums.FeedbackAction;
import ovi.web.dhybe.enums.FeedbackStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackHistoryItem {
    private Long id;
    private FeedbackAction action;
    private FeedbackStatus oldStatus;
    private FeedbackStatus newStatus;
    private String note;
    private Long userId;
    private String userName;
    private LocalDateTime createdDate;
}





