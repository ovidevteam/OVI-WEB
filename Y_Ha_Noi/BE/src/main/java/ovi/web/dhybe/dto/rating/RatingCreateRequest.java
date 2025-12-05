package ovi.web.dhybe.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RatingCreateRequest {

    @NotNull
    private Long feedbackId;

//    @NotNull
    private Long doctorId;

    @Min(1)
    @Max(5)
    private int rating;

    private String comment;
}





