package ovi.web.dhybe.dto.feedback;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ovi.web.dhybe.enums.FeedbackStatus;

import java.util.List;

@Data
public class FeedbackProcessingRequest {
    @NotNull
    private FeedbackStatus status;

    private String note;

    /**
     * List of image identifiers returned after uploading via FileUpload API.
     */
    private List<Long> imageIds;
}





