package ovi.web.dhybe.dto.report;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardReportResponse {
    private long totalFeedbacks;
    private long newFeedbacks;
    private long inProgressFeedbacks;
    private long completedFeedbacks;
    private long overdueFeedbacks;
    private double averageProcessingDays;
    private Map<String, Long> levelDistribution;
    private Map<String, Long> channelDistribution;
}





