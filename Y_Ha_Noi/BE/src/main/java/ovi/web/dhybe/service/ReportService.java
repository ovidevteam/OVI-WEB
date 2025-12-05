package ovi.web.dhybe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ovi.web.dhybe.dto.report.*;
import ovi.web.dhybe.entity.Department;
import ovi.web.dhybe.entity.Doctor;
import ovi.web.dhybe.entity.Feedback;
import ovi.web.dhybe.entity.FeedbackImage;
import ovi.web.dhybe.enums.FeedbackImageType;
import ovi.web.dhybe.enums.FeedbackStatus;
import ovi.web.dhybe.mapper.ReportMapper;
import ovi.web.dhybe.repository.FeedbackImageRepository;
import ovi.web.dhybe.repository.FeedbackRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final FeedbackRepository feedbackRepository;
    private final DepartmentService departmentService;
    private final DoctorService doctorService;
    private final FeedbackImageRepository feedbackImageRepository;
    private final ReportMapper reportMapper;

    @Transactional(readOnly = true)
    public DashboardReportResponse dashboard() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        long total = feedbacks.size();
        long newCount = countByStatus(feedbacks, FeedbackStatus.NEW);
        long inProgress = countByStatus(feedbacks, FeedbackStatus.IN_PROGRESS);
        long completed = countByStatus(feedbacks, FeedbackStatus.COMPLETED);
        long overdue = feedbacks.stream()
                .filter(feedback -> feedback.getStatus() != FeedbackStatus.COMPLETED)
                .filter(feedback -> feedback.getReceivedDate().isBefore(LocalDateTime.now().minusDays(3)))
                .count();
        double avgProcessDays = feedbacks.stream()
                .filter(feedback -> feedback.getCompletedDate() != null)
                .mapToDouble(feedback ->
                        java.time.Duration.between(feedback.getReceivedDate(), feedback.getCompletedDate()).toDays())
                .average()
                .orElse(0);
        Map<String, Long> levelDistribution = feedbacks.stream()
                .collect(Collectors.groupingBy(feedback -> feedback.getLevel().name(), Collectors.counting()));
        Map<String, Long> channelDistribution = feedbacks.stream()
                .collect(Collectors.groupingBy(Feedback::getChannel, Collectors.counting()));

        return DashboardReportResponse.builder()
                .totalFeedbacks(total)
                .newFeedbacks(newCount)
                .inProgressFeedbacks(inProgress)
                .completedFeedbacks(completed)
                .overdueFeedbacks(overdue)
                .averageProcessingDays(avgProcessDays)
                .levelDistribution(levelDistribution)
                .channelDistribution(channelDistribution)
                .build();
    }

    @Transactional(readOnly = true)
    public List<DepartmentReportItem> reportByDepartment() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        Map<Long, List<Feedback>> byDepartment = feedbacks.stream()
                .filter(feedback -> feedback.getDepartment() != null)
                .collect(Collectors.groupingBy(feedback -> feedback.getDepartment().getId()));
        return byDepartment.entrySet().stream()
                .map(entry -> {
                    Department department = departmentService.getEntity(entry.getKey());
                    List<Feedback> values = entry.getValue();
                    long total = values.size();
                    long completed = countByStatus(values, FeedbackStatus.COMPLETED);
                    long inProgress = countByStatus(values, FeedbackStatus.IN_PROGRESS);
                    long overdue = values.stream()
                            .filter(feedback -> feedback.getStatus() != FeedbackStatus.COMPLETED)
                            .count();
                    double avgRating = values.stream()
                            .filter(feedback -> feedback.getRating() != null)
                            .mapToInt(feedback -> feedback.getRating().getRating())
                            .average()
                            .orElse(0);
                    return reportMapper.toDepartmentReportItem(department, total, completed, inProgress, overdue, avgRating);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DoctorReportItem> reportByDoctor() {
        List<Feedback> feedbacks = feedbackRepository.findAll();
        Map<Long, List<Feedback>> byDoctor = feedbacks.stream()
                .filter(feedback -> feedback.getDoctor() != null)
                .collect(Collectors.groupingBy(feedback -> feedback.getDoctor().getId()));
        return byDoctor.entrySet().stream()
                .map(entry -> {
                    Doctor doctor = doctorService.getEntity(entry.getKey());
                    List<Feedback> values = entry.getValue();
                    long total = values.size();
                    long completed = countByStatus(values, FeedbackStatus.COMPLETED);
                    double avgRating = values.stream()
                            .filter(feedback -> feedback.getRating() != null)
                            .mapToInt(feedback -> feedback.getRating().getRating())
                            .average()
                            .orElse(0);
                    return reportMapper.toDoctorReportItem(doctor, total, completed, avgRating);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportWithImagesItem> reportWithImages(Long departmentId, FeedbackImageType type) {
        List<Feedback> filtered = feedbackRepository.findAll().stream()
                .filter(feedback -> departmentId == null ||
                        (feedback.getDepartment() != null && feedback.getDepartment().getId().equals(departmentId)))
                .filter(feedback -> !feedback.getImages().isEmpty())
                .collect(Collectors.toList());
        boolean includeFeedbackImages = type == null || type == FeedbackImageType.FEEDBACK;
        boolean includeProcessImages = type == null || type == FeedbackImageType.PROCESS;
        return filtered.stream()
                .map(feedback -> {
                    List<FeedbackImage> images = feedbackImageRepository.findByFeedback_Id(feedback.getId());
                    List<FeedbackImage> feedbackImages = includeFeedbackImages
                            ? images.stream()
                            .filter(image -> image.getImageType() == FeedbackImageType.FEEDBACK)
                            .collect(Collectors.toList())
                            : List.of();
                    List<FeedbackImage> processImages = includeProcessImages
                            ? images.stream()
                            .filter(image -> image.getImageType() == FeedbackImageType.PROCESS)
                            .collect(Collectors.toList())
                            : List.of();
                    if (type == FeedbackImageType.FEEDBACK && feedbackImages.isEmpty()) {
                        return null;
                    }
                    if (type == FeedbackImageType.PROCESS && processImages.isEmpty()) {
                        return null;
                    }
                    return reportMapper.toReportWithImagesItem(feedback, feedbackImages, processImages);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MonthlyStatsItem> monthlyStats(int year) {
        List<Feedback> feedbacks = feedbackRepository.findAll().stream()
                .filter(feedback -> feedback.getReceivedDate().getYear() == year)
                .collect(Collectors.toList());
        Map<Month, List<Feedback>> byMonth = feedbacks.stream()
                .collect(Collectors.groupingBy(feedback -> feedback.getReceivedDate().getMonth()));
        List<MonthlyStatsItem> stats = new ArrayList<>();
        for (Month month : Month.values()) {
            List<Feedback> monthData = byMonth.getOrDefault(month, List.of());
            long total = monthData.size();
            long completed = countByStatus(monthData, FeedbackStatus.COMPLETED);
            long inProgress = countByStatus(monthData, FeedbackStatus.IN_PROGRESS);
            long overdue = monthData.stream()
                    .filter(feedback -> feedback.getStatus() != FeedbackStatus.COMPLETED)
                    .count();
            double avgProcessDays = monthData.stream()
                    .filter(feedback -> feedback.getCompletedDate() != null)
                    .mapToDouble(feedback ->
                            java.time.Duration.between(feedback.getReceivedDate(), feedback.getCompletedDate()).toDays())
                    .average()
                    .orElse(0);
            Map<String, Long> byLevel = monthData.stream()
                    .collect(Collectors.groupingBy(feedback -> feedback.getLevel().name(), Collectors.counting()));
            stats.add(MonthlyStatsItem.builder()
                    .month(month.getValue())
                    .monthName("Tháng " + month.getValue())
                    .total(total)
                    .completed(completed)
                    .inProgress(inProgress)
                    .overdue(overdue)
                    .avgProcessDays(avgProcessDays)
                    .byLevel(byLevel)
                    .build());
        }
        return stats;
    }

    private long countByStatus(List<Feedback> feedbacks, FeedbackStatus status) {
        return feedbacks.stream()
                .filter(feedback -> feedback.getStatus() == status)
                .count();
    }
}

