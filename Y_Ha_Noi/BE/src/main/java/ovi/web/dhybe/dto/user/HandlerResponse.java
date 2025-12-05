package ovi.web.dhybe.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HandlerResponse {
    private Long id;
    private String fullName;
    private String email;
    private Long departmentId;
    private String departmentName;
    private String role;
}





