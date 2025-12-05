package ovi.web.dhybe.dto.department;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ovi.web.dhybe.enums.EntityStatus;

@Data
public class DepartmentRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private Long managerId;

    private Long defaultHandlerId;

    @Email
    private String notificationEmail;

    private EntityStatus status = EntityStatus.ACTIVE;
}





