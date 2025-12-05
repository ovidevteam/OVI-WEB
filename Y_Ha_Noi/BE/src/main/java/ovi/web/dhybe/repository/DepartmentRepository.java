package ovi.web.dhybe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ovi.web.dhybe.entity.Department;
import ovi.web.dhybe.enums.EntityStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Department> findByStatus(EntityStatus status);
}





