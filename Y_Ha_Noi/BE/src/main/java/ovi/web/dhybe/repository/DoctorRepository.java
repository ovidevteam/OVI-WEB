package ovi.web.dhybe.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ovi.web.dhybe.entity.Doctor;
import ovi.web.dhybe.enums.EntityStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    List<Doctor> findByDepartmentId(Long departmentId);

    List<Doctor> findByDepartmentIdAndStatus(Long departmentId, EntityStatus status);
}





