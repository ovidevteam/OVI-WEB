package com.bvyhanoi.feedback.repository;

import com.bvyhanoi.feedback.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByCode(String code);
    List<Department> findByStatus(Department.DepartmentStatus status);
    boolean existsByCode(String code);
}

