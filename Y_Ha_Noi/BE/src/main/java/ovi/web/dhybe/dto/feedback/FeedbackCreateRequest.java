package ovi.web.dhybe.dto.feedback;

import lombok.Data;
import ovi.web.dhybe.enums.FeedbackLevel;

import java.time.LocalDateTime;

@Data
public class FeedbackCreateRequest {

    private String code;

    private LocalDateTime receivedDate;

    private String channel;

    private String content;

    private Long departmentId;

    private Long doctorId;

    private FeedbackLevel level;

    private Long receiverId;
}





