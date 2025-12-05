package ovi.web.dhybe.dto.report;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentReportItem {
    private Long departmentId;
    private String departmentName;
    private long total;
    private long completed;
    private long inProgress;
    private long overdue;
    private double avgRating;
}





