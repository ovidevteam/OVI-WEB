package ovi.web.dhybe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ovi.web.dhybe.dto.common.ApiResponse;
import ovi.web.dhybe.dto.doctor.DoctorRequest;
import ovi.web.dhybe.dto.doctor.DoctorResponse;
import ovi.web.dhybe.enums.EntityStatus;
import ovi.web.dhybe.service.DoctorService;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LEADER','RECEIVER')")
    public List<DoctorResponse> list(@RequestParam(required = false) Long departmentId,
                                     @RequestParam(required = false) EntityStatus status) {
        return doctorService.list(departmentId, status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER','RECEIVER')")
    public DoctorResponse get(@PathVariable Long id) {
        return doctorService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorResponse create(@Valid @RequestBody DoctorRequest request) {
        return doctorService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorResponse update(@PathVariable Long id, @Valid @RequestBody DoctorRequest request) {
        return doctorService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        doctorService.delete(id);
        return ApiResponse.ok("Doctor deleted");
    }
}

