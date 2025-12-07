package com.bvyhanoi.feedback.controller;

import com.bvyhanoi.feedback.dto.DashboardStatsDTO;
import com.bvyhanoi.feedback.dto.FeedbackDTO;
import com.bvyhanoi.feedback.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reports")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public ResponseEntity<DashboardStatsDTO> getDashboard() {
        DashboardStatsDTO stats = reportService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/monthly-stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public ResponseEntity<List<DashboardStatsDTO.MonthlyStat>> getMonthlyStats(@RequestParam(required = false) Integer year) {
        List<DashboardStatsDTO.MonthlyStat> stats = reportService.getMonthlyStats(year);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/by-department")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public ResponseEntity<List<Map<String, Object>>> getByDepartment(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer limit) {
        List<Map<String, Object>> stats = reportService.getByDepartment(dateFrom, dateTo, departmentId);
        if (limit != null && limit > 0) {
            stats = stats.stream().limit(limit).collect(java.util.stream.Collectors.toList());
        }
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/by-doctor")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public ResponseEntity<List<Map<String, Object>>> getByDoctor(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long departmentId) {
        List<Map<String, Object>> stats = reportService.getByDoctor(dateFrom, dateTo, departmentId);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/with-images")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public ResponseEntity<List<FeedbackDTO>> getWithImages(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long departmentId) {
        List<FeedbackDTO> feedbacks = reportService.getFeedbacksWithImages(dateFrom, dateTo, departmentId);
        return ResponseEntity.ok(feedbacks);
    }
    
    @GetMapping("/export-excel")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String type) {
        // TODO: Implement Excel export
        byte[] emptyData = new byte[0];
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "report.xlsx");
        return ResponseEntity.ok().headers(headers).body(emptyData);
    }
    
    @GetMapping("/export-pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String type) {
        // TODO: Implement PDF export
        byte[] emptyData = new byte[0];
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "report.pdf");
        return ResponseEntity.ok().headers(headers).body(emptyData);
    }
}

