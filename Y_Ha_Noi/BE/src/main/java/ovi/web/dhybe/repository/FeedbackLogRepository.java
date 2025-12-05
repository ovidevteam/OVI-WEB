package ovi.web.dhybe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ovi.web.dhybe.entity.FeedbackLog;

import java.util.List;

@Repository
public interface FeedbackLogRepository extends JpaRepository<FeedbackLog, Long> {

    List<FeedbackLog> findByFeedback_IdOrderByCreatedDateAsc(Long feedbackId);
}





