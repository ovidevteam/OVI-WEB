package com.bvyhanoi.feedback.repository;

import com.bvyhanoi.feedback.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByCode(String code);
    List<Doctor> findByDepartmentId(Long departmentId);
    List<Doctor> findByDepartmentIdAndStatus(Long departmentId, Doctor.DoctorStatus status);
    Page<Doctor> findByDepartmentId(Long departmentId, Pageable pageable);
    
    @Query(value = "SELECT * FROM doctors d WHERE " +
           "(LOWER(COALESCE(d.full_name, '')) LIKE LOWER(:keywordPattern) OR " +
           "LOWER(COALESCE(d.code, '')) LIKE LOWER(:keywordPattern)) AND " +
           "(:departmentId IS NULL OR d.department_id = :departmentId) " +
           "ORDER BY d.created_at DESC",
           countQuery = "SELECT COUNT(*) FROM doctors d WHERE " +
           "(LOWER(COALESCE(d.full_name, '')) LIKE LOWER(:keywordPattern) OR " +
           "LOWER(COALESCE(d.code, '')) LIKE LOWER(:keywordPattern)) AND " +
           "(:departmentId IS NULL OR d.department_id = :departmentId)",
           nativeQuery = true)
    Page<Doctor> search(@Param("keywordPattern") String keywordPattern, 
                       @Param("departmentId") Long departmentId, 
                       Pageable pageable);
    
    boolean existsByCode(String code);
}

