package com.bvyhanoi.feedback.repository;

import com.bvyhanoi.feedback.entity.Rating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findFirstByFeedbackId(Long feedbackId); // Get first rating for backward compatibility
    Optional<Rating> findByFeedbackIdAndUserId(Long feedbackId, Long userId); // Get rating by user
    List<Rating> findAllByFeedbackId(Long feedbackId); // Get all ratings for a feedback
    List<Rating> findByDoctorId(Long doctorId);
    Page<Rating> findByDoctorId(Long doctorId, Pageable pageable);
    
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.doctorId = :doctorId")
    Double getAverageRatingByDoctorId(@Param("doctorId") Long doctorId);
    
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.doctorId = :doctorId")
    Long countByDoctorId(@Param("doctorId") Long doctorId);
}

