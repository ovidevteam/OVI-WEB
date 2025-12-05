package ovi.web.dhybe.dto.feedback;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FeedbackAssignRequest {
    @NotNull
    private Long handlerId;
    private String note;
}





