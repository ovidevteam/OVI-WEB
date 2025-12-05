package ovi.web.dhybe.mapper;

import org.springframework.stereotype.Component;
import ovi.web.dhybe.dto.feedback.FeedbackImageResponse;
import ovi.web.dhybe.dto.report.DepartmentReportItem;
import ovi.web.dhybe.dto.report.DoctorReportItem;
import ovi.web.dhybe.dto.report.ReportWithImagesItem;
import ovi.web.dhybe.entity.Department;
import ovi.web.dhybe.entity.Doctor;
import ovi.web.dhybe.entity.Feedback;
import ovi.web.dhybe.entity.FeedbackImage;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportMapper {

    private final FeedbackMapper feedbackMapper;

    public ReportMapper(FeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    public DepartmentReportItem toDepartmentReportItem(Department department,
                                                       long total,
                                                       long completed,
                                                       long inProgress,
                                                       long overdue,
                                                       double avgRating) {
        return DepartmentReportItem.builder()
                .departmentId(department.getId())
                .departmentName(department.getName())
                .total(total)
                .completed(completed)
                .inProgress(inProgress)
                .overdue(overdue)
                .avgRating(avgRating)
                .build();
    }

    public DoctorReportItem toDoctorReportItem(Doctor doctor,
                                               long total,
                                               long completed,
                                               double avgRating) {
        return DoctorReportItem.builder()
                .doctorId(doctor.getId())
                .doctorName(doctor.getFullName())
                .departmentName(doctor.getDepartment() != null ? doctor.getDepartment().getName() : null)
                .total(total)
                .completed(completed)
                .avgRating(avgRating)
                .build();
    }

    public ReportWithImagesItem toReportWithImagesItem(Feedback feedback,
                                                       List<FeedbackImage> feedbackImages,
                                                       List<FeedbackImage> processImages) {
        return ReportWithImagesItem.builder()
                .feedbackId(feedback.getId())
                .code(feedback.getCode())
                .content(feedback.getContent())
                .departmentName(feedback.getDepartment() != null ? feedback.getDepartment().getName() : null)
                .status(feedback.getStatus().name())
                .feedbackImages(toImageResponses(feedbackImages))
                .processImages(toImageResponses(processImages))
                .build();
    }

    private List<FeedbackImageResponse> toImageResponses(List<FeedbackImage> images) {
        return images.stream()
                .map(feedbackMapper::toImageResponse)
                .collect(Collectors.toList());
    }
}





