package ovi.web.dhybe.dto.report;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class MonthlyStatsItem {
    private int month;
    private String monthName;
    private long total;
    private long completed;
    private long inProgress;
    private long overdue;
    private double avgProcessDays;
    private Map<String, Long> byLevel;
}





