package ovi.web.dhybe.dto.feedback;

import lombok.Builder;
import lombok.Data;
import ovi.web.dhybe.enums.FeedbackImageType;

import java.time.LocalDateTime;

@Data
@Builder
public class FeedbackImageResponse {
    private Long id;
    private String path;
    // Public URL used by FE to display the image
    private String url;
    private FeedbackImageType imageType;
    private Long uploadedBy;
    private String uploadedByName;
    private LocalDateTime uploadedDate;
}





