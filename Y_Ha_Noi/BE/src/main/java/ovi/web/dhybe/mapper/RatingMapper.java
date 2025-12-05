package ovi.web.dhybe.mapper;

import org.springframework.stereotype.Component;
import ovi.web.dhybe.dto.rating.CompletedFeedbackResponse;
import ovi.web.dhybe.dto.rating.DoctorRatingSummary;
import ovi.web.dhybe.dto.rating.RatingResponse;
import ovi.web.dhybe.entity.Department;
import ovi.web.dhybe.entity.Doctor;
import ovi.web.dhybe.entity.Feedback;
import ovi.web.dhybe.entity.Rating;
import ovi.web.dhybe.dto.auth.UserDto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RatingMapper {

    public RatingResponse toResponse(Rating rating) {
        Feedback feedback = rating.getFeedback();
        Doctor doctor = rating.getDoctor();
        UserDto rater = rating.getRatedBy();
        return RatingResponse.builder()
                .id(rating.getId())
                .feedbackId(feedback != null ? feedback.getId() : null)
                .feedbackCode(feedback != null ? feedback.getCode() : null)
                .doctorId(doctor != null ? doctor.getId() : null)
                .doctorName(doctor != null ? doctor.getFullName() : null)
                .rating(rating.getRating())
                .comment(rating.getComment())
                .ratedBy(rater != null ? (long) rater.getId() : null)
                .ratedByName(rater != null ? rater.getFullName() : null)
                .ratedDate(rating.getRatedDate())
                .build();
    }

    public CompletedFeedbackResponse toCompletedFeedback(Feedback feedback) {
        Doctor doctor = feedback.getDoctor();
        Department department = feedback.getDepartment();
        Rating rating = feedback.getRating();
        return CompletedFeedbackResponse.builder()
                .id(feedback.getId())
                .code(feedback.getCode())
                .content(feedback.getContent())
                .doctorId(doctor != null ? doctor.getId() : null)
                .doctorName(doctor != null ? doctor.getFullName() : null)
                .departmentId(department != null ? department.getId() : null)
                .departmentName(department != null ? department.getName() : null)
                .completedDate(feedback.getCompletedDate())
                .rating(rating != null ? rating.getRating() : null)
                .comment(rating != null ? rating.getComment() : null)
                .build();
    }

    public List<DoctorRatingSummary> toTopDoctorSummaries(List<Object[]> tuples,
                                                          Map<Long, Doctor> doctorLookup) {
        return tuples.stream()
                .map(tuple -> {
                    Long doctorId = (Long) tuple[0];
                    Double avg = (Double) tuple[1];
                    Long total = (Long) tuple[2];
                    Doctor doctor = doctorLookup.get(doctorId);
                    Department department = doctor != null ? doctor.getDepartment() : null;
                    return DoctorRatingSummary.builder()
                            .doctorId(doctorId)
                            .doctorName(doctor != null ? doctor.getFullName() : null)
                            .departmentName(department != null ? department.getName() : null)
                            .average(avg != null ? avg : 0)
                            .totalRatings(total != null ? total : 0)
                            .build();
                })
                .collect(Collectors.toList());
    }
}

