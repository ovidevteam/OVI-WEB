package ovi.web.dhybe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ovi.web.dhybe.dto.common.ApiResponse;
import ovi.web.dhybe.dto.department.DepartmentRequest;
import ovi.web.dhybe.dto.department.DepartmentResponse;
import ovi.web.dhybe.enums.EntityStatus;
import ovi.web.dhybe.service.DepartmentService;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    public List<DepartmentResponse> list(@RequestParam(required = false) EntityStatus status) {
        return departmentService.list(status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    public DepartmentResponse get(@PathVariable Long id) {
        return departmentService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public DepartmentResponse create(@Valid @RequestBody DepartmentRequest request, Authentication authentication) {
        return departmentService.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DepartmentResponse update(@PathVariable Long id,
                                     @Valid @RequestBody DepartmentRequest request,
                                     Authentication authentication) {
        return departmentService.update(id, request, authentication.getName());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.ok("Department deleted");
    }
}

