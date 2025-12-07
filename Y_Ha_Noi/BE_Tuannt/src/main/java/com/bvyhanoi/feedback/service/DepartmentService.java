package com.bvyhanoi.feedback.service;

import com.bvyhanoi.feedback.dto.CreateDepartmentRequest;
import com.bvyhanoi.feedback.dto.DepartmentDTO;
import com.bvyhanoi.feedback.entity.Department;
import com.bvyhanoi.feedback.exception.ResourceNotFoundException;
import com.bvyhanoi.feedback.exception.ValidationException;
import com.bvyhanoi.feedback.repository.DepartmentRepository;
import com.bvyhanoi.feedback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public List<DepartmentDTO> getAllDepartments() {
        return departmentRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    public List<DepartmentDTO> getActiveDepartments() {
        return departmentRepository.findByStatus(Department.DepartmentStatus.ACTIVE).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    public DepartmentDTO getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        return toDTO(department);
    }
    
    @Transactional
    public DepartmentDTO createDepartment(CreateDepartmentRequest request) {
        // Generate code if not provided or empty
        String code = request.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = generateDepartmentCode();
        }
        
        if (departmentRepository.existsByCode(code)) {
            throw new ValidationException("Department code already exists");
        }
        
        Department department = new Department();
        department.setCode(code);
        department.setName(request.getName());
        department.setDescription(request.getDescription());
        department.setManagerId(request.getManagerId());
        department.setHandlerId(request.getDefaultHandlerId());
        department.setNotificationEmail(request.getNotificationEmail());
        department.setStatus(request.getStatus() != null ? request.getStatus() : Department.DepartmentStatus.ACTIVE);
        
        return toDTO(departmentRepository.save(department));
    }
    
    /**
     * Generate department code in format PB-XXX
     * Finds the highest existing code and increments it
     */
    private String generateDepartmentCode() {
        List<Department> allDepartments = departmentRepository.findAll();
        int maxNumber = 0;
        
        for (Department dept : allDepartments) {
            if (dept.getCode() != null && dept.getCode().startsWith("PB-")) {
                try {
                    String numberPart = dept.getCode().substring(3); // Skip "PB-"
                    int number = Integer.parseInt(numberPart);
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    // Ignore codes that don't match pattern
                }
            }
        }
        
        int nextNumber = maxNumber + 1;
        return String.format("PB-%03d", nextNumber);
    }
    
    @Transactional
    public DepartmentDTO updateDepartment(Long id, CreateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        
        if (request.getCode() != null && !department.getCode().equals(request.getCode()) && departmentRepository.existsByCode(request.getCode())) {
            throw new ValidationException("Department code already exists");
        }
        
        if (request.getCode() != null) {
            department.setCode(request.getCode());
        }
        if (request.getName() != null) {
            department.setName(request.getName());
        }
        if (request.getDescription() != null) {
            department.setDescription(request.getDescription());
        }
        department.setManagerId(request.getManagerId());
        department.setHandlerId(request.getDefaultHandlerId());
        if (request.getNotificationEmail() != null) {
            department.setNotificationEmail(request.getNotificationEmail());
        }
        if (request.getStatus() != null) {
            department.setStatus(request.getStatus());
        }
        
        return toDTO(departmentRepository.save(department));
    }
    
    @Transactional
    public void deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Department", id));
        departmentRepository.delete(department);
    }
    
    private DepartmentDTO toDTO(Department department) {
        DepartmentDTO dto = new DepartmentDTO();
        dto.setId(department.getId());
        dto.setCode(department.getCode());
        dto.setName(department.getName());
        dto.setDescription(department.getDescription());
        dto.setManagerId(department.getManagerId());
        dto.setHandlerId(department.getHandlerId());
        dto.setNotificationEmail(department.getNotificationEmail());
        dto.setStatus(department.getStatus());
        dto.setCreatedAt(department.getCreatedAt());
        dto.setUpdatedAt(department.getUpdatedAt());
        
        // Map manager name
        if (department.getManagerId() != null) {
            userRepository.findById(department.getManagerId())
                .ifPresent(user -> dto.setManagerName(user.getFullName()));
        }
        
        // Map default handler name
        if (department.getHandlerId() != null) {
            userRepository.findById(department.getHandlerId())
                .ifPresent(user -> dto.setDefaultHandlerName(user.getFullName()));
        }
        
        return dto;
    }
}

