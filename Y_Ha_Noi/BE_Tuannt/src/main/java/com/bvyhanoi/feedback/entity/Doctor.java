package com.bvyhanoi.feedback.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    @Column(length = 100)
    private String specialty;
    
    @Column(name = "department_id", nullable = false)
    private Long departmentId;
    
    @Column(length = 100)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DoctorStatus status = DoctorStatus.ACTIVE;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum DoctorStatus {
        ACTIVE, INACTIVE
    }
}

