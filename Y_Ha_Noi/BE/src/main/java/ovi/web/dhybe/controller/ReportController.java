package ovi.web.dhybe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ovi.web.dhybe.dto.report.*;
import ovi.web.dhybe.enums.FeedbackImageType;
import ovi.web.dhybe.service.ReportService;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    public DashboardReportResponse dashboard() {
        return reportService.dashboard();
    }

    @GetMapping("/by-department")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    public List<DepartmentReportItem> byDepartment() {
        return reportService.reportByDepartment();
    }

    @GetMapping("/by-doctor")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    public List<DoctorReportItem> byDoctor() {
        return reportService.reportByDoctor();
    }

    @GetMapping("/with-images")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    public List<ReportWithImagesItem> withImages(@RequestParam(required = false) Long departmentId,
                                                 @RequestParam(required = false) FeedbackImageType imageType) {
        return reportService.reportWithImages(departmentId, imageType);
    }

    @GetMapping("/monthly-stats")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    public List<MonthlyStatsItem> monthlyStats(@RequestParam int year) {
        return reportService.monthlyStats(year);
    }
}

