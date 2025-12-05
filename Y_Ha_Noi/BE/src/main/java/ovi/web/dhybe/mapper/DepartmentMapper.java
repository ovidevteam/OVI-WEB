package ovi.web.dhybe.mapper;

import org.springframework.stereotype.Component;
import ovi.web.dhybe.dto.department.DepartmentRequest;
import ovi.web.dhybe.dto.department.DepartmentResponse;
import ovi.web.dhybe.entity.Department;

@Component
public class DepartmentMapper {

    public DepartmentResponse toResponse(Department entity) {
        if (entity == null) {
            return null;
        }
        return DepartmentResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .managerId(entity.getManagerId())
                .defaultHandlerId(entity.getDefaultHandlerId())
                .notificationEmail(entity.getNotificationEmail())
                .status(entity.getStatus())
                .createdDate(entity.getCreatedDate())
                .modifyDate(entity.getModifyDate())
                .build();
    }

    public void updateEntity(Department entity, DepartmentRequest request) {
        entity.setCode(request.getCode());
        entity.setName(request.getName());
        entity.setManagerId(request.getManagerId());
        entity.setDefaultHandlerId(request.getDefaultHandlerId());
        entity.setNotificationEmail(request.getNotificationEmail());
        entity.setStatus(request.getStatus());
    }
}





