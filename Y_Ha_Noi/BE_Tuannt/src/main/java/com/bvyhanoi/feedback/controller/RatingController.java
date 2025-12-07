package com.bvyhanoi.feedback.controller;

import com.bvyhanoi.feedback.dto.CreateRatingRequest;
import com.bvyhanoi.feedback.dto.FeedbackDTO;
import com.bvyhanoi.feedback.dto.PagedResponse;
import com.bvyhanoi.feedback.dto.RatingDTO;
import com.bvyhanoi.feedback.service.RatingService;
import com.bvyhanoi.feedback.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ratings")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class RatingController {
    
    @Autowired
    private RatingService ratingService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping("/completed-feedbacks")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'HANDLER')")
    public ResponseEntity<PagedResponse<FeedbackDTO>> getCompletedFeedbacks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Boolean hasRating,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long doctorId) {
        Long userId = authService.getCurrentUser().getId();
        PagedResponse<FeedbackDTO> response = ratingService.getCompletedFeedbacks(page, size, hasRating, departmentId, doctorId, userId);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'HANDLER')")
    public ResponseEntity<RatingDTO> createRating(@Valid @RequestBody CreateRatingRequest request) {
        Long userId = authService.getCurrentUser().getId();
        RatingDTO rating = ratingService.createRating(request, userId);
        return ResponseEntity.ok(rating);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'HANDLER')")
    public ResponseEntity<RatingDTO> updateRating(@PathVariable Long id, @Valid @RequestBody CreateRatingRequest request) {
        Long userId = authService.getCurrentUser().getId();
        RatingDTO rating = ratingService.updateRating(id, request, userId);
        return ResponseEntity.ok(rating);
    }
    
    @GetMapping("/by-feedback/{feedbackId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER', 'HANDLER')")
    public ResponseEntity<RatingDTO> getRatingByFeedback(@PathVariable Long feedbackId) {
        Long userId = authService.getCurrentUser().getId();
        RatingDTO rating = ratingService.getRatingByFeedback(feedbackId, userId);
        if (rating == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(rating);
    }
    
    @GetMapping("/by-feedback/{feedbackId}/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public ResponseEntity<List<RatingDTO>> getAllRatingsByFeedback(@PathVariable Long feedbackId) {
        List<RatingDTO> ratings = ratingService.getAllRatingsByFeedback(feedbackId);
        return ResponseEntity.ok(ratings);
    }
    
    @GetMapping("/doctor/{doctorId}/average")
    public ResponseEntity<Map<String, Object>> getDoctorAverageRating(@PathVariable Long doctorId) {
        Map<String, Object> result = ratingService.getDoctorAverageRating(doctorId);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<PagedResponse<RatingDTO>> getDoctorRatings(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<RatingDTO> response = ratingService.getDoctorRatings(doctorId, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long departmentId) {
        Map<String, Object> result = ratingService.getStatistics(dateFrom, dateTo, departmentId);
        return ResponseEntity.ok(result);
    }
}

