package ovi.web.dhybe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ovi.web.dhybe.entity.Feedback;
import ovi.web.dhybe.enums.FeedbackStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long>, JpaSpecificationExecutor<Feedback> {

    Optional<Feedback> findByCode(String code);

    boolean existsByCode(String code);

    List<Feedback> findByHandler_Id(Long handlerId);

    List<Feedback> findByReceiver_Id(Long receiverId);

    List<Feedback> findByDoctor_Id(Long doctorId);

    List<Feedback> findByStatus(FeedbackStatus status);
}





