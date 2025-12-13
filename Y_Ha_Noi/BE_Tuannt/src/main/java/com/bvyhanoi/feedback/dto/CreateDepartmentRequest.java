package com.bvyhanoi.feedback.dto;

import com.bvyhanoi.feedback.entity.Department;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDepartmentRequest {
    private String code; // Optional - will be auto-generated if empty
    
    @NotBlank(message = "Name is required")
    private String name;
    
    private String description;
    private Long managerId;
    private Long defaultHandlerId;
    
    @Email(message = "Invalid email format")
    private String notificationEmail;
    
    private Department.DepartmentStatus status;
}

