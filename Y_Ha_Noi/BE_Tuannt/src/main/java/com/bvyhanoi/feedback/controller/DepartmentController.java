package com.bvyhanoi.feedback.controller;

import com.bvyhanoi.feedback.dto.CreateDepartmentRequest;
import com.bvyhanoi.feedback.dto.DepartmentDTO;
import com.bvyhanoi.feedback.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/departments")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DepartmentController {
    
    @Autowired
    private DepartmentService departmentService;
    
    @GetMapping
    public ResponseEntity<List<DepartmentDTO>> getDepartments(@RequestParam(required = false) String status) {
        List<DepartmentDTO> departments;
        if ("ACTIVE".equals(status)) {
            departments = departmentService.getActiveDepartments();
        } else {
            departments = departmentService.getAllDepartments();
        }
        return ResponseEntity.ok(departments);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDTO> getDepartmentById(@PathVariable Long id) {
        DepartmentDTO department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(department);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDTO> createDepartment(@Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentDTO department = departmentService.createDepartment(request);
        return ResponseEntity.ok(department);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentDTO> updateDepartment(@PathVariable Long id, @Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentDTO department = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(department);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        return ResponseEntity.ok(response);
    }
}

