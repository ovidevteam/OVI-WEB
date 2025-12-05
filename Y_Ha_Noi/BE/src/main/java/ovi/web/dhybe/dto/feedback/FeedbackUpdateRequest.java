package ovi.web.dhybe.dto.feedback;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ovi.web.dhybe.enums.FeedbackLevel;
import ovi.web.dhybe.enums.FeedbackStatus;

@Data
public class FeedbackUpdateRequest {
//    @NotNull
    private Long id;
    private String content;
    private Long departmentId;
    private Long doctorId;
    private FeedbackLevel level;
    private FeedbackStatus status;
    private String processNote;
}





