package ovi.web.dhybe.mapper;

import org.springframework.stereotype.Component;
import ovi.web.dhybe.dto.doctor.DoctorRequest;
import ovi.web.dhybe.dto.doctor.DoctorResponse;
import ovi.web.dhybe.entity.Department;
import ovi.web.dhybe.entity.Doctor;

@Component
public class DoctorMapper {

    public DoctorResponse toResponse(Doctor entity) {
        if (entity == null) {
            return null;
        }
        Department department = entity.getDepartment();
        return DoctorResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .fullName(entity.getFullName())
                .specialty(entity.getSpecialty())
                .departmentId(department != null ? department.getId() : null)
                .departmentName(department != null ? department.getName() : null)
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .createdDate(entity.getCreatedDate())
                .build();
    }

    public void updateEntity(Doctor entity, DoctorRequest request, Department department) {
        entity.setCode(request.getCode());
        entity.setFullName(request.getFullName());
        entity.setSpecialty(request.getSpecialty());
        entity.setDepartment(department);
        entity.setEmail(request.getEmail());
        entity.setPhone(request.getPhone());
        entity.setStatus(request.getStatus());
    }
}





