package com.bvyhanoi.feedback.repository;

import com.bvyhanoi.feedback.entity.FeedbackHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackHistoryRepository extends JpaRepository<FeedbackHistory, Long> {
    List<FeedbackHistory> findByFeedbackIdOrderByCreatedAtDesc(Long feedbackId);
}

