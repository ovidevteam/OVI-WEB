package ovi.web.dhybe.dto.upload;

import lombok.Builder;
import lombok.Data;
import ovi.web.dhybe.enums.FeedbackImageType;

@Data
@Builder
public class FileUploadResponse {
    private Long id;
    private String fileName;
    private String url;
    private FeedbackImageType imageType;
}





