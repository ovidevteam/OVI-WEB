package ovi.web.dhybe.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ovi.web.dhybe.entity.Rating;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByFeedback_Id(Long feedbackId);

    Page<Rating> findByDoctor_Id(Long doctorId, Pageable pageable);

    List<Rating> findByDoctor_Id(Long doctorId);

    Long countByDoctor_Id(Long doctorId);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.doctor.id = :doctorId")
    Double getAverageRatingByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT r.doctor.id, AVG(r.rating) AS avgRating, COUNT(r) AS totalRatings " +
            "FROM Rating r GROUP BY r.doctor.id ORDER BY avgRating DESC")
    List<Object[]> getTopDoctorsByRating(Pageable pageable);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.ratedDate BETWEEN :start AND :end")
    Long countByRatedDateBetween(@Param("start") LocalDateTime start,
                                 @Param("end") LocalDateTime end);
}

