package ovi.web.dhybe.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ovi.web.dhybe.dto.auth.UserDto;
import ovi.web.dhybe.dto.common.PageResponse;
import ovi.web.dhybe.dto.rating.*;
import ovi.web.dhybe.entity.Doctor;
import ovi.web.dhybe.entity.Feedback;
import ovi.web.dhybe.entity.Rating;
import ovi.web.dhybe.enums.FeedbackStatus;
import ovi.web.dhybe.mapper.RatingMapper;
import ovi.web.dhybe.repository.FeedbackRepository;
import ovi.web.dhybe.repository.RatingRepository;
import ovi.web.dhybe.repository.UserDtoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final FeedbackRepository feedbackRepository;
    private final DoctorService doctorService;
    private final FeedbackService feedbackService;
    private final UserDtoRepository userDtoRepository;
    private final RatingMapper ratingMapper;

    @Transactional(readOnly = true)
    public List<CompletedFeedbackResponse> listCompletedFeedbacks(boolean ratedOnly) {
        List<Feedback> feedbacks = ratedOnly
                ? feedbackRepository.findByStatus(FeedbackStatus.COMPLETED).stream()
                .filter(feedback -> feedback.getRating() != null)
                .collect(Collectors.toList())
                : feedbackService.listCompletedWithoutRating();
        return feedbacks.stream()
                .map(ratingMapper::toCompletedFeedback)
                .collect(Collectors.toList());
    }

    @Transactional
    public RatingResponse create(Long actorId, RatingCreateRequest request) {
        Feedback feedback = feedbackRepository.findById(request.getFeedbackId())
                .orElseThrow(() -> new EntityNotFoundException("Feedback not found: " + request.getFeedbackId()));
        if (feedback.getStatus() != FeedbackStatus.COMPLETED) {
            throw new IllegalStateException("Feedback must be completed before rating");
        }
        if (feedback.getRating() != null) {
            throw new IllegalStateException("Feedback already has a rating");
        }
        Doctor doctor = doctorService.getEntity(request.getDoctorId());
        if (feedback.getDoctor() == null || !feedback.getDoctor().getId().equals(doctor.getId())) {
            throw new IllegalArgumentException("Doctor does not match the feedback assignment");
        }
        UserDto actor = getUser(actorId);
        Rating rating = Rating.builder()
                .feedback(feedback)
                .doctor(doctor)
                .rating(request.getRating())
                .comment(request.getComment())
                .ratedBy(actor)
                .createdBy(actor.getUsername())
                .build();
        Rating saved = ratingRepository.save(rating);
        return ratingMapper.toResponse(saved);
    }

    @Transactional
    public RatingResponse update(Long ratingId, RatingUpdateRequest request, Long actorId) {
        Rating rating = getEntity(ratingId);
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        UserDto actor = getUser(actorId);
        rating.setModifiedBy(actor.getUsername());
        return ratingMapper.toResponse(rating);
    }

    @Transactional(readOnly = true)
    public RatingResponse getByFeedback(Long feedbackId) {
        return ratingRepository.findByFeedback_Id(feedbackId)
                .map(ratingMapper::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public PageResponse<RatingResponse> doctorRatings(Long doctorId, Pageable pageable) {
        Page<RatingResponse> mapped = ratingRepository.findByDoctor_Id(doctorId, pageable)
                .map(ratingMapper::toResponse);
        return PageResponse.from(mapped);
    }

    @Transactional(readOnly = true)
    public RatingStatisticsResponse statistics(LocalDate from, LocalDate to, int topDoctorLimit) {
        LocalDateTime start = from != null ? from.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
        LocalDateTime end = to != null ? to.atTime(LocalTime.MAX) : LocalDateTime.now();
        List<Rating> ratings = ratingRepository.findAll();
        List<Rating> filtered = ratings.stream()
                .filter(r -> r.getRatedDate() == null ||
                        (!r.getRatedDate().isBefore(start) && !r.getRatedDate().isAfter(end)))
                .collect(Collectors.toList());
        long total = ratings.size();
        double avg = filtered.stream()
                .mapToInt(Rating::getRating)
                .average()
                .orElse(0);
        Map<Integer, Long> distribution = filtered.stream()
                .collect(Collectors.groupingBy(Rating::getRating, Collectors.counting()));
        List<Object[]> topTuples = ratingRepository.getTopDoctorsByRating(Pageable.ofSize(topDoctorLimit));
        Map<Long, Doctor> doctorLookup = topTuples.stream()
                .map(tuple -> (Long) tuple[0])
                .distinct()
                .map(doctorService::getEntity)
                .collect(Collectors.toMap(Doctor::getId, doctor -> doctor));
        List<DoctorRatingSummary> topDoctors = ratingMapper.toTopDoctorSummaries(topTuples, doctorLookup);
        Map<Integer, Long> distFilled = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distFilled.put(i, distribution.getOrDefault(i, 0L));
        }
        return RatingStatisticsResponse.builder()
                .totalRatings(total)
                .averageRating(avg)
                .distribution(distFilled)
                .topDoctors(topDoctors)
                .build();
    }

    private Rating getEntity(Long id) {
        return ratingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rating not found: " + id));
    }

    private UserDto getUser(Long id) {
        return userDtoRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }
}

