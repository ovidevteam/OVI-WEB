package com.bvyhanoi.feedback.service;

import com.bvyhanoi.feedback.dto.CreateDoctorRequest;
import com.bvyhanoi.feedback.dto.DoctorDTO;
import com.bvyhanoi.feedback.dto.PagedResponse;
import com.bvyhanoi.feedback.entity.Doctor;
import com.bvyhanoi.feedback.exception.ResourceNotFoundException;
import com.bvyhanoi.feedback.exception.ValidationException;
import com.bvyhanoi.feedback.repository.DepartmentRepository;
import com.bvyhanoi.feedback.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    public PagedResponse<DoctorDTO> getAllDoctors(int page, int size, String keyword, Long departmentId) {
        // Normalize parameters
        String searchKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Long searchDepartmentId = (departmentId != null && departmentId > 0) ? departmentId : null;
        
        Page<Doctor> doctorPage;
        
        // Use different query based on whether keyword is provided
        if (searchKeyword != null) {
            // Format keyword pattern for LIKE query
            String keywordPattern = "%" + searchKeyword + "%";
            // Native query handles sorting internally, so use unsorted Pageable
            Pageable pageable = PageRequest.of(page - 1, size);
            doctorPage = doctorRepository.search(keywordPattern, searchDepartmentId, pageable);
        } else {
            // No keyword - use simpler query with sorting
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            if (searchDepartmentId != null) {
                doctorPage = doctorRepository.findByDepartmentId(searchDepartmentId, pageable);
            } else {
                // No filters - get all
                doctorPage = doctorRepository.findAll(pageable);
            }
        }
        
        List<DoctorDTO> doctorDTOs = doctorPage.getContent().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        
        return PagedResponse.of(doctorDTOs, doctorPage.getTotalElements(), page, size);
    }
    
    public List<DoctorDTO> getDoctorsByDepartment(Long departmentId) {
        return doctorRepository.findByDepartmentIdAndStatus(departmentId, Doctor.DoctorStatus.ACTIVE).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    public DoctorDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        return toDTO(doctor);
    }
    
    @Transactional
    public DoctorDTO createDoctor(CreateDoctorRequest request) {
        // Generate code if not provided or empty
        String code = request.getCode();
        if (code == null || code.trim().isEmpty()) {
            code = generateDoctorCode(request.getSpecialty());
        }
        
        // Check if code already exists
        if (doctorRepository.existsByCode(code)) {
            throw new ValidationException("Doctor code already exists: " + code);
        }
        
        Doctor doctor = new Doctor();
        doctor.setCode(code);
        doctor.setFullName(request.getFullName());
        doctor.setSpecialty(request.getSpecialty());
        doctor.setDepartmentId(request.getDepartmentId());
        doctor.setEmail(request.getEmail());
        doctor.setPhone(request.getPhone());
        doctor.setStatus(Doctor.DoctorStatus.ACTIVE);
        
        return toDTO(doctorRepository.save(doctor));
    }
    
    /**
     * Generate doctor code based on specialty
     * Format: BS-{SPECIALTY_PREFIX}-{SEQUENCE}
     * Example: BS-NK-001 (Nhi khoa), BS-SK-001 (Sản khoa)
     */
    private String generateDoctorCode(String specialty) {
        if (specialty == null || specialty.trim().isEmpty()) {
            throw new ValidationException("Specialty is required to generate doctor code");
        }
        
        // Extract prefix from specialty (first letter of each word)
        // Example: "Nhi khoa" -> "NK", "Sản khoa" -> "SK", "Nội khoa" -> "NK"
        String trimmedSpecialty = specialty.trim();
        if (trimmedSpecialty.isEmpty()) {
            throw new ValidationException("Specialty cannot be empty after trimming");
        }
        
        // Split by spaces and take first letter of each word
        String[] words = trimmedSpecialty.split("\\s+");
        StringBuilder prefixBuilder = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                prefixBuilder.append(word.charAt(0));
            }
        }
        String specialtyPrefix = prefixBuilder.toString().toUpperCase();
        
        // If only one word, take first 2 letters
        if (specialtyPrefix.length() < 2 && trimmedSpecialty.length() >= 2) {
            specialtyPrefix = trimmedSpecialty.substring(0, 2).toUpperCase();
        }
        
        // Find the highest sequence number for this specialty
        List<Doctor> doctorsWithSameSpecialty = doctorRepository.findAll().stream()
            .filter(d -> d.getSpecialty() != null && d.getSpecialty().equals(specialty))
            .collect(java.util.stream.Collectors.toList());
        
        int maxSequence = 0;
        String codePattern = "BS-" + specialtyPrefix + "-";
        
        for (Doctor doctor : doctorsWithSameSpecialty) {
            if (doctor.getCode() != null && doctor.getCode().startsWith(codePattern)) {
                try {
                    String seqStr = doctor.getCode().substring(codePattern.length());
                    int seq = Integer.parseInt(seqStr);
                    if (seq > maxSequence) {
                        maxSequence = seq;
                    }
                } catch (NumberFormatException e) {
                    // Ignore invalid codes
                }
            }
        }
        
        // Generate next sequence number (3 digits, zero-padded)
        int nextSequence = maxSequence + 1;
        return codePattern + String.format("%03d", nextSequence);
    }
    
    @Transactional
    public DoctorDTO updateDoctor(Long id, CreateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        
        if (!doctor.getCode().equals(request.getCode()) && doctorRepository.existsByCode(request.getCode())) {
            throw new ValidationException("Doctor code already exists");
        }
        
        doctor.setCode(request.getCode());
        doctor.setFullName(request.getFullName());
        doctor.setSpecialty(request.getSpecialty());
        doctor.setDepartmentId(request.getDepartmentId());
        doctor.setEmail(request.getEmail());
        doctor.setPhone(request.getPhone());
        
        return toDTO(doctorRepository.save(doctor));
    }
    
    @Transactional
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        doctorRepository.delete(doctor);
    }
    
    private DoctorDTO toDTO(Doctor doctor) {
        DoctorDTO dto = new DoctorDTO();
        dto.setId(doctor.getId());
        dto.setCode(doctor.getCode());
        dto.setFullName(doctor.getFullName());
        dto.setSpecialty(doctor.getSpecialty());
        dto.setDepartmentId(doctor.getDepartmentId());
        dto.setEmail(doctor.getEmail());
        dto.setPhone(doctor.getPhone());
        dto.setStatus(doctor.getStatus());
        dto.setCreatedAt(doctor.getCreatedAt());
        dto.setUpdatedAt(doctor.getUpdatedAt());
        
        // Map departmentName from departmentId
        if (doctor.getDepartmentId() != null) {
            departmentRepository.findById(doctor.getDepartmentId())
                .ifPresent(dept -> dto.setDepartmentName(dept.getName()));
        }
        
        return dto;
    }
}

