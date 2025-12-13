package com.bvyhanoi.feedback.dto;

import com.bvyhanoi.feedback.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private User.Role role;
    private Long departmentId;
    private String departmentName;
    private User.UserStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

