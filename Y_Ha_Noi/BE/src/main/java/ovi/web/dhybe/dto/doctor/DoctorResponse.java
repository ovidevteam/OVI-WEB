package ovi.web.dhybe.dto.doctor;

import lombok.Builder;
import lombok.Data;
import ovi.web.dhybe.enums.EntityStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class DoctorResponse {
    private Long id;
    private String code;
    private String fullName;
    private String specialty;
    private Long departmentId;
    private String departmentName;
    private String email;
    private String phone;
    private EntityStatus status;
    private LocalDateTime createdDate;
}





