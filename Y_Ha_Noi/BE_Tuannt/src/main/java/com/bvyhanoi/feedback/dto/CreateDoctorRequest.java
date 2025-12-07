package com.bvyhanoi.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateDoctorRequest {
    private String code; // Optional - will be auto-generated if empty
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @NotBlank(message = "Specialty is required")
    private String specialty;
    
    @NotNull(message = "Department ID is required")
    private Long departmentId;
    
    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Invalid email format")
    private String email;
    
    private String phone;
}

