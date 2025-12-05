package ovi.web.dhybe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ovi.web.dhybe.entity.FeedbackImage;
import ovi.web.dhybe.enums.FeedbackImageType;

import java.util.List;

@Repository
public interface FeedbackImageRepository extends JpaRepository<FeedbackImage, Long> {

    List<FeedbackImage> findByFeedback_Id(Long feedbackId);

    List<FeedbackImage> findByFeedback_IdAndImageType(Long feedbackId, FeedbackImageType type);
}





