package ovi.web.dhybe.dto.rating;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class RatingStatisticsResponse {
    private long totalRatings;
    private double averageRating;
    private Map<Integer, Long> distribution;
    private List<DoctorRatingSummary> topDoctors;
}





