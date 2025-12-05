package ovi.web.dhybe.dto.feedback;

import lombok.Builder;
import lombok.Data;
import ovi.web.dhybe.enums.FeedbackLevel;
import ovi.web.dhybe.enums.FeedbackStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class FeedbackDetailResponse {
    private Long id;
    private String code;
    private FeedbackStatus status;
    private FeedbackLevel level;
    private String content;
    private String channel;
    private Long departmentId;
    private String departmentName;
    private Long doctorId;
    private String doctorName;
    private Long handlerId;
    private String handlerName;
    private Long receiverId;
    private String receiverName;
    private LocalDateTime receivedDate;
    private LocalDateTime completedDate;
    private String processNote;
    private Integer processCount;
    private LocalDateTime assignedDate;
    private LocalDateTime lastProcessDate;
    private List<FeedbackImageResponse> images;
    // Backend field name kept for compatibility
    private List<FeedbackHistoryItem> history;
    // Alias used by FE: feedback.logs
    private List<FeedbackHistoryItem> logs;
}





