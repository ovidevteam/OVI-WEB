package com.bvyhanoi.feedback.dto;

import com.bvyhanoi.feedback.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @NotNull(message = "Role is required")
    private User.Role role;
    
    private Long departmentId;
}

