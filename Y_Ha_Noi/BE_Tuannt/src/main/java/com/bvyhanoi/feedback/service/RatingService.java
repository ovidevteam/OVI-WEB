package com.bvyhanoi.feedback.service;

import com.bvyhanoi.feedback.dto.CreateRatingRequest;
import com.bvyhanoi.feedback.dto.FeedbackDTO;
import com.bvyhanoi.feedback.dto.PagedResponse;
import com.bvyhanoi.feedback.dto.RatingDTO;
import com.bvyhanoi.feedback.entity.Feedback;
import com.bvyhanoi.feedback.entity.Rating;
import com.bvyhanoi.feedback.exception.ResourceNotFoundException;
import com.bvyhanoi.feedback.exception.ValidationException;
import com.bvyhanoi.feedback.entity.Doctor;
import com.bvyhanoi.feedback.entity.Department;
import com.bvyhanoi.feedback.entity.User;
import com.bvyhanoi.feedback.repository.FeedbackRepository;
import com.bvyhanoi.feedback.repository.RatingRepository;
import com.bvyhanoi.feedback.repository.DoctorRepository;
import com.bvyhanoi.feedback.repository.DepartmentRepository;
import com.bvyhanoi.feedback.repository.UserRepository;
import com.bvyhanoi.feedback.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RatingService {
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AuthService authService;
    
    public PagedResponse<FeedbackDTO> getCompletedFeedbacks(int page, int size, Boolean hasRating, Long departmentId, Long doctorId, Long userId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("completedDate").descending());
        
        List<Feedback> completedFeedbacks = feedbackRepository.findByStatus(Feedback.FeedbackStatus.COMPLETED);
        
        List<FeedbackDTO> filteredFeedbacks = completedFeedbacks.stream()
            .filter(f -> departmentId == null || f.getDepartmentId() != null && f.getDepartmentId().equals(departmentId))
            .filter(f -> doctorId == null || f.getDoctorId() != null && f.getDoctorId().equals(doctorId))
            .filter(f -> {
                if (hasRating == null) return true;
                // Check if current user has rated this feedback
                boolean hasRatingValue = userId != null && ratingRepository.findByFeedbackIdAndUserId(f.getId(), userId).isPresent();
                return hasRating == hasRatingValue;
            })
            .skip((long) (page - 1) * size)
            .limit(size)
            .map(f -> {
                FeedbackDTO dto = new FeedbackDTO();
                dto.setId(f.getId());
                dto.setCode(f.getCode());
                dto.setContent(f.getContent());
                dto.setStatus(f.getStatus());
                dto.setCompletedDate(f.getCompletedDate());
                dto.setDepartmentId(f.getDepartmentId());
                dto.setDoctorId(f.getDoctorId());
                
                // Calculate average rating from all users
                List<Rating> allRatings = ratingRepository.findAllByFeedbackId(f.getId());
                if (!allRatings.isEmpty()) {
                    double avgRating = allRatings.stream()
                        .mapToInt(Rating::getRating)
                        .average()
                        .orElse(0.0);
                    dto.setAverageRating(Math.round(avgRating * 10.0) / 10.0); // Round to 1 decimal
                }
                
                // Load rating of current user if exists
                boolean userHasRated = false;
                if (userId != null) {
                    Optional<Rating> userRating = ratingRepository.findByFeedbackIdAndUserId(f.getId(), userId);
                    if (userRating.isPresent()) {
                        userHasRated = true;
                        dto.setRating(userRating.get().getRating());
                        dto.setComment(userRating.get().getComment());
                    }
                }
                dto.setUserHasRated(userHasRated);
                
                // Load doctor name
                if (f.getDoctorId() != null) {
                    doctorRepository.findById(f.getDoctorId()).ifPresent(doctor -> {
                        dto.setDoctorName(doctor.getFullName());
                    });
                }
                
                // Load department name
                if (f.getDepartmentId() != null) {
                    departmentRepository.findById(f.getDepartmentId()).ifPresent(department -> {
                        dto.setDepartmentName(department.getName());
                    });
                }
                
                return dto;
            })
            .collect(Collectors.toList());
        
        long total = completedFeedbacks.stream()
            .filter(f -> departmentId == null || f.getDepartmentId() != null && f.getDepartmentId().equals(departmentId))
            .filter(f -> doctorId == null || f.getDoctorId() != null && f.getDoctorId().equals(doctorId))
            .filter(f -> {
                if (hasRating == null) return true;
                // Check if current user has rated this feedback
                boolean hasRatingValue = userId != null && ratingRepository.findByFeedbackIdAndUserId(f.getId(), userId).isPresent();
                return hasRating == hasRatingValue;
            })
            .count();
        
        return PagedResponse.of(filteredFeedbacks, total, page, size);
    }
    
    @Transactional
    public RatingDTO createRating(CreateRatingRequest request, Long userId) {
        Feedback feedback = feedbackRepository.findById(request.getFeedbackId())
            .orElseThrow(() -> new ResourceNotFoundException("Feedback", request.getFeedbackId()));
        
        if (feedback.getStatus() != Feedback.FeedbackStatus.COMPLETED) {
            throw new ValidationException("Feedback must be completed before rating");
        }
        
        // Check if user already rated this feedback
        if (ratingRepository.findByFeedbackIdAndUserId(request.getFeedbackId(), userId).isPresent()) {
            throw new ValidationException("Bạn đã đánh giá phản ánh này. Vui lòng sử dụng chức năng cập nhật.");
        }
        
        Rating rating = new Rating();
        rating.setFeedbackId(request.getFeedbackId());
        rating.setUserId(userId);
        rating.setDoctorId(request.getDoctorId() != null ? request.getDoctorId() : feedback.getDoctorId());
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        
        Rating savedRating = ratingRepository.save(rating);
        
        // Create notification for ADMIN and LEADER
        User user = userRepository.findById(userId).orElse(null);
        String userName = user != null ? user.getFullName() : "Người dùng";
        notificationService.createRatingNotification(
            feedback.getId(), 
            savedRating.getId(), 
            feedback.getCode(), 
            userName
        );
        
        return toDTO(savedRating);
    }
    
    @Transactional
    public RatingDTO updateRating(Long id, CreateRatingRequest request, Long userId) {
        Rating rating = ratingRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Rating", id));
        
        // Only allow user to update their own rating
        if (!rating.getUserId().equals(userId)) {
            throw new ValidationException("Bạn chỉ có thể sửa đánh giá của chính mình.");
        }
        
        rating.setRating(request.getRating());
        rating.setComment(request.getComment());
        
        return toDTO(ratingRepository.save(rating));
    }
    
    public RatingDTO getRatingByFeedback(Long feedbackId, Long userId) {
        return ratingRepository.findByFeedbackIdAndUserId(feedbackId, userId)
            .map(this::toDTO)
            .orElse(null);
    }
    
    // Get all ratings for a feedback (for admin/leader to see all ratings)
    public List<RatingDTO> getAllRatingsByFeedback(Long feedbackId) {
        return ratingRepository.findAllByFeedbackId(feedbackId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    public Map<String, Object> getDoctorAverageRating(Long doctorId) {
        Double avgRating = ratingRepository.getAverageRatingByDoctorId(doctorId);
        Long totalRatings = ratingRepository.countByDoctorId(doctorId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("avgRating", avgRating != null ? avgRating : 0.0);
        result.put("totalRatings", totalRatings != null ? totalRatings : 0L);
        return result;
    }
    
    public PagedResponse<RatingDTO> getDoctorRatings(Long doctorId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Rating> ratingPage = ratingRepository.findByDoctorId(doctorId, pageable);
        
        List<RatingDTO> ratingDTOs = ratingPage.getContent().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        
        return PagedResponse.of(ratingDTOs, ratingPage.getTotalElements(), page, size);
    }
    
    public Map<String, Object> getStatistics(LocalDate dateFrom, LocalDate dateTo, Long departmentId) {
        List<Rating> allRatings = ratingRepository.findAll();
        
        List<Rating> filteredRatings = allRatings.stream()
            .filter(r -> {
                Feedback feedback = feedbackRepository.findById(r.getFeedbackId()).orElse(null);
                if (feedback == null) return false;
                if (dateFrom != null && feedback.getCompletedDate() != null && feedback.getCompletedDate().isBefore(dateFrom)) return false;
                if (dateTo != null && feedback.getCompletedDate() != null && feedback.getCompletedDate().isAfter(dateTo)) return false;
                if (departmentId != null && feedback.getDepartmentId() != null && !feedback.getDepartmentId().equals(departmentId)) return false;
                return true;
            })
            .collect(Collectors.toList());
        
        double avgRating = filteredRatings.stream()
            .mapToInt(Rating::getRating)
            .average()
            .orElse(0.0);
        
        Map<Integer, Long> ratingDistribution = filteredRatings.stream()
            .collect(Collectors.groupingBy(Rating::getRating, Collectors.counting()));
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalRatings", (long) filteredRatings.size());
        result.put("avgRating", avgRating);
        result.put("ratingDistribution", ratingDistribution);
        
        return result;
    }
    
    private RatingDTO toDTO(Rating rating) {
        RatingDTO dto = new RatingDTO();
        dto.setId(rating.getId());
        dto.setFeedbackId(rating.getFeedbackId());
        dto.setUserId(rating.getUserId());
        dto.setDoctorId(rating.getDoctorId());
        dto.setRating(rating.getRating());
        dto.setComment(rating.getComment());
        dto.setCreatedAt(rating.getCreatedAt());
        dto.setUpdatedAt(rating.getUpdatedAt());
        
        // Load user name
        if (rating.getUserId() != null) {
            userRepository.findById(rating.getUserId()).ifPresent(user -> {
                dto.setUserName(user.getFullName());
            });
        }
        
        return dto;
    }
}

