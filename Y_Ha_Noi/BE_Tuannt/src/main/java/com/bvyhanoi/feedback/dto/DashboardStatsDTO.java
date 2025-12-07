package com.bvyhanoi.feedback.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private Stats stats;
    private List<MonthlyStat> monthlyStats;
    private List<DepartmentStat> departmentStats;
    private List<FeedbackDTO> recentFeedbacks;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Stats {
        private Long total;
        private Long pending;
        private Long processing;
        private Long completed;
        private Long overdue;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStat {
        private Integer month;
        private Long count;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DepartmentStat {
        private String departmentName;
        private Long count;
    }
}

