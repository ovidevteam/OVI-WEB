package com.bvyhanoi.feedback.controller;

import com.bvyhanoi.feedback.dto.CreateDoctorRequest;
import com.bvyhanoi.feedback.dto.DoctorDTO;
import com.bvyhanoi.feedback.dto.PagedResponse;
import com.bvyhanoi.feedback.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctors")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DoctorController {
    
    @Autowired
    private DoctorService doctorService;
    
    @GetMapping
    public ResponseEntity<?> getDoctors(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean listOnly) {
        // If listOnly is true and departmentId is provided, return list without pagination
        if (Boolean.TRUE.equals(listOnly) && departmentId != null) {
            List<DoctorDTO> doctors = doctorService.getDoctorsByDepartment(departmentId);
            return ResponseEntity.ok(doctors);
        }
        // Otherwise, return paginated response
        PagedResponse<DoctorDTO> response = doctorService.getAllDoctors(page, size, keyword, departmentId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long id) {
        DoctorDTO doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorDTO> createDoctor(@Valid @RequestBody CreateDoctorRequest request) {
        DoctorDTO doctor = doctorService.createDoctor(request);
        return ResponseEntity.ok(doctor);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorDTO> updateDoctor(@PathVariable Long id, @Valid @RequestBody CreateDoctorRequest request) {
        DoctorDTO doctor = doctorService.updateDoctor(id, request);
        return ResponseEntity.ok(doctor);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        return ResponseEntity.ok(response);
    }
}

