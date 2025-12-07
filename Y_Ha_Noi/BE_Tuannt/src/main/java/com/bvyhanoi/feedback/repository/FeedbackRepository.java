package com.bvyhanoi.feedback.repository;

import com.bvyhanoi.feedback.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByCode(String code);
    
    @Query("SELECT f FROM Feedback f WHERE f.handlerId = :handlerId ORDER BY f.code DESC")
    List<Feedback> findByHandlerId(@Param("handlerId") Long handlerId);
    
    @Query("SELECT f FROM Feedback f WHERE f.status = :status ORDER BY f.code DESC")
    List<Feedback> findByStatus(@Param("status") Feedback.FeedbackStatus status);
    
    @Query(value = "SELECT * FROM feedbacks f WHERE " +
           "(:status IS NULL OR f.status = CAST(:status AS VARCHAR)) AND " +
           "(:level IS NULL OR f.level = CAST(:level AS VARCHAR)) AND " +
           "(:channel IS NULL OR f.channel = CAST(:channel AS VARCHAR)) AND " +
           "(:departmentId IS NULL OR f.department_id = :departmentId) AND " +
           "(f.received_date >= COALESCE(CAST(:dateFromStr AS DATE), CAST('1900-01-01' AS DATE))) AND " +
           "(f.received_date <= COALESCE(CAST(:dateToStr AS DATE), CAST('9999-12-31' AS DATE))) AND " +
           "(:keywordPattern IS NULL OR CAST(f.content AS TEXT) LIKE :keywordPattern OR " +
           "f.code LIKE :keywordPattern) " +
           "ORDER BY f.code DESC",
           countQuery = "SELECT COUNT(*) FROM feedbacks f WHERE " +
           "(:status IS NULL OR f.status = CAST(:status AS VARCHAR)) AND " +
           "(:level IS NULL OR f.level = CAST(:level AS VARCHAR)) AND " +
           "(:channel IS NULL OR f.channel = CAST(:channel AS VARCHAR)) AND " +
           "(:departmentId IS NULL OR f.department_id = :departmentId) AND " +
           "(f.received_date >= COALESCE(CAST(:dateFromStr AS DATE), CAST('1900-01-01' AS DATE))) AND " +
           "(f.received_date <= COALESCE(CAST(:dateToStr AS DATE), CAST('9999-12-31' AS DATE))) AND " +
           "(:keywordPattern IS NULL OR CAST(f.content AS TEXT) LIKE :keywordPattern OR " +
           "f.code LIKE :keywordPattern)",
           nativeQuery = true)
    Page<Feedback> search(@Param("status") String status,
                         @Param("level") String level,
                         @Param("channel") String channel,
                         @Param("departmentId") Long departmentId,
                         @Param("dateFromStr") String dateFromStr,
                         @Param("dateToStr") String dateToStr,
                         @Param("keyword") String keyword,
                         @Param("keywordPattern") String keywordPattern,
                         Pageable pageable);
    
    @Query(value = "SELECT * FROM feedbacks f WHERE " +
           "(:status IS NULL OR f.status = CAST(:status AS VARCHAR)) AND " +
           "(:level IS NULL OR f.level = CAST(:level AS VARCHAR)) AND " +
           "(:channel IS NULL OR f.channel = CAST(:channel AS VARCHAR)) AND " +
           "(:departmentId IS NULL OR f.department_id = :departmentId) AND " +
           "f.doctor_id = :doctorId AND " +
           "(f.received_date >= COALESCE(CAST(:dateFromStr AS DATE), CAST('1900-01-01' AS DATE))) AND " +
           "(f.received_date <= COALESCE(CAST(:dateToStr AS DATE), CAST('9999-12-31' AS DATE))) AND " +
           "(:keywordPattern IS NULL OR CAST(f.content AS TEXT) LIKE :keywordPattern OR " +
           "f.code LIKE :keywordPattern) " +
           "ORDER BY f.code DESC",
           countQuery = "SELECT COUNT(*) FROM feedbacks f WHERE " +
           "(:status IS NULL OR f.status = CAST(:status AS VARCHAR)) AND " +
           "(:level IS NULL OR f.level = CAST(:level AS VARCHAR)) AND " +
           "(:channel IS NULL OR f.channel = CAST(:channel AS VARCHAR)) AND " +
           "(:departmentId IS NULL OR f.department_id = :departmentId) AND " +
           "f.doctor_id = :doctorId AND " +
           "(f.received_date >= COALESCE(CAST(:dateFromStr AS DATE), CAST('1900-01-01' AS DATE))) AND " +
           "(f.received_date <= COALESCE(CAST(:dateToStr AS DATE), CAST('9999-12-31' AS DATE))) AND " +
           "(:keywordPattern IS NULL OR CAST(f.content AS TEXT) LIKE :keywordPattern OR " +
           "f.code LIKE :keywordPattern)",
           nativeQuery = true)
    Page<Feedback> searchWithDoctor(@Param("status") String status,
                                   @Param("level") String level,
                                   @Param("channel") String channel,
                                   @Param("departmentId") Long departmentId,
                                   @Param("doctorId") Long doctorId,
                                   @Param("dateFromStr") String dateFromStr,
                                   @Param("dateToStr") String dateToStr,
                                   @Param("keyword") String keyword,
                                   @Param("keywordPattern") String keywordPattern,
                                   Pageable pageable);
    
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.status = :status")
    Long countByStatus(@Param("status") Feedback.FeedbackStatus status);
    
    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.receivedDate >= :dateFrom AND f.receivedDate <= :dateTo")
    Long countByDateRange(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo);
    
    boolean existsByCode(String code);
    
    @Query("SELECT f.code FROM Feedback f WHERE f.code LIKE :datePattern ORDER BY f.code DESC")
    List<String> findCodesByDatePattern(@Param("datePattern") String datePattern);
}

