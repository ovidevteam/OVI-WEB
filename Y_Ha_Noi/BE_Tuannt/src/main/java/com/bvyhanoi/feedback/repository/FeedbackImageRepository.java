package com.bvyhanoi.feedback.repository;

import com.bvyhanoi.feedback.entity.FeedbackImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackImageRepository extends JpaRepository<FeedbackImage, Long> {
    List<FeedbackImage> findByFeedbackId(Long feedbackId);
    List<FeedbackImage> findByFeedbackIdAndImageType(Long feedbackId, FeedbackImage.ImageType imageType);
}

