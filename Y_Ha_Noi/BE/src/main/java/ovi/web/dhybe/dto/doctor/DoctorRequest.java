package ovi.web.dhybe.dto.doctor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ovi.web.dhybe.enums.EntityStatus;

@Data
public class DoctorRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String fullName;

    private String specialty;

    @NotNull
    private Long departmentId;

    @Email
    private String email;

    private String phone;

    private EntityStatus status = EntityStatus.ACTIVE;
}





