package ovi.web.dhybe.dto.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorReportItem {
    private Long doctorId;
    private String doctorName;
    private String departmentName;
    private long total;
    private long completed;
    private double avgRating;
}





