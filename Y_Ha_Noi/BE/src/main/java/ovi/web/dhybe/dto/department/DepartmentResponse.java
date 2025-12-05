package ovi.web.dhybe.dto.department;

import lombok.Builder;
import lombok.Data;
import ovi.web.dhybe.enums.EntityStatus;

import java.time.LocalDateTime;

@Data
@Builder
public class DepartmentResponse {
    private Long id;
    private String code;
    private String name;
    private Long managerId;
    private Long defaultHandlerId;
    private String notificationEmail;
    private EntityStatus status;
    private LocalDateTime createdDate;
    private LocalDateTime modifyDate;
}





