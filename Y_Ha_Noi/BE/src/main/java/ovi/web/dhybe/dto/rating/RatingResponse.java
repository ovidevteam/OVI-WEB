package ovi.web.dhybe.dto.rating;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RatingResponse {
    private Long id;
    private Long feedbackId;
    private String feedbackCode;
    private Long doctorId;
    private String doctorName;
    private Integer rating;
    private String comment;
    private Long ratedBy;
    private String ratedByName;
    private LocalDateTime ratedDate;
}





