package ovi.web.dhybe.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ovi.web.dhybe.dto.department.DepartmentRequest;
import ovi.web.dhybe.dto.department.DepartmentResponse;
import ovi.web.dhybe.entity.Department;
import ovi.web.dhybe.enums.EntityStatus;
import ovi.web.dhybe.mapper.DepartmentMapper;
import ovi.web.dhybe.repository.DepartmentRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    @Transactional
    public DepartmentResponse create(DepartmentRequest request, String actor) {
        validateUniqueCode(request.getCode(), null);
        Department department = Department.builder().build();
        departmentMapper.updateEntity(department, request);
        department.setCreatedBy(actor);
        department.setModifiedBy(actor);
        Department saved = departmentRepository.save(department);
        return departmentMapper.toResponse(saved);
    }

    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request, String actor) {
        Department department = getEntity(id);
        validateUniqueCode(request.getCode(), id);
        departmentMapper.updateEntity(department, request);
        department.setModifiedBy(actor);
        return departmentMapper.toResponse(department);
    }

    @Transactional(readOnly = true)
    public DepartmentResponse get(Long id) {
        return departmentMapper.toResponse(getEntity(id));
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> list(EntityStatus status) {
        List<Department> departments = status == null
                ? departmentRepository.findAll()
                : departmentRepository.findByStatus(status);
        return departments.stream()
                .map(departmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long id) {
        Department department = getEntity(id);
        departmentRepository.delete(department);
    }

    public Department getEntity(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Department not found: " + id));
    }

    private void validateUniqueCode(String code, Long currentId) {
        departmentRepository.findByCodeIgnoreCase(code)
                .filter(existing -> !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Department code already exists: " + code);
                });
    }
}





