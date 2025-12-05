package ovi.web.dhybe.dto.rating;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorRatingSummary {
    private Long doctorId;
    private String doctorName;
    private String departmentName;
    private double average;
    private long totalRatings;
}





