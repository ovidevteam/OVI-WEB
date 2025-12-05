package ovi.web.dhybe.dto.rating;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RatingUpdateRequest {
    @Min(1)
    @Max(5)
    private int rating;
    private String comment;
}





