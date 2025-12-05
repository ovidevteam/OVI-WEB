package ovi.web.dhybe.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ovi.web.dhybe.dto.doctor.DoctorRequest;
import ovi.web.dhybe.dto.doctor.DoctorResponse;
import ovi.web.dhybe.entity.Department;
import ovi.web.dhybe.entity.Doctor;
import ovi.web.dhybe.enums.EntityStatus;
import ovi.web.dhybe.mapper.DoctorMapper;
import ovi.web.dhybe.repository.DoctorRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DepartmentService departmentService;
    private final DoctorMapper doctorMapper;

    @Transactional
    public DoctorResponse create(DoctorRequest request) {
        validateUniqueCode(request.getCode(), null);
        Department department = departmentService.getEntity(request.getDepartmentId());
        Doctor doctor = Doctor.builder().build();
        doctorMapper.updateEntity(doctor, request, department);
        Doctor saved = doctorRepository.save(doctor);
        return doctorMapper.toResponse(saved);
    }

    @Transactional
    public DoctorResponse update(Long id, DoctorRequest request) {
        Doctor doctor = getEntity(id);
        validateUniqueCode(request.getCode(), id);
        Department department = departmentService.getEntity(request.getDepartmentId());
        doctorMapper.updateEntity(doctor, request, department);
        return doctorMapper.toResponse(doctor);
    }

    @Transactional(readOnly = true)
    public DoctorResponse get(Long id) {
        return doctorMapper.toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> list(Long departmentId, EntityStatus status) {
        List<Doctor> doctors;
        if (departmentId != null && status != null) {
            doctors = doctorRepository.findByDepartmentIdAndStatus(departmentId, status);
        } else if (departmentId != null) {
            doctors = doctorRepository.findByDepartmentId(departmentId);
        } else {
            doctors = doctorRepository.findAll();
        }
        return doctors.stream()
                .map(doctorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id) {
        Doctor doctor = getEntity(id);
        doctorRepository.delete(doctor);
    }

    public Doctor getEntity(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Doctor not found: " + id));
    }

    private void validateUniqueCode(String code, Long currentId) {
        doctorRepository.findByCodeIgnoreCase(code)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Doctor code already exists: " + code);
                });
    }
}





