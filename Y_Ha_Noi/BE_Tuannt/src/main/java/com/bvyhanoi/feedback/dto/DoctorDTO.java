package com.bvyhanoi.feedback.dto;

import com.bvyhanoi.feedback.entity.Doctor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DoctorDTO {
    private Long id;
    private String code;
    private String fullName;
    private String specialty;
    private Long departmentId;
    private String departmentName;
    private String email;
    private String phone;
    private Doctor.DoctorStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

