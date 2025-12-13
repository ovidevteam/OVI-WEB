package com.bvyhanoi.feedback.dto;

import com.bvyhanoi.feedback.entity.Department;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DepartmentDTO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long managerId;
    private String managerName;
    private Long handlerId;
    private String defaultHandlerName;
    private String notificationEmail;
    private Department.DepartmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

