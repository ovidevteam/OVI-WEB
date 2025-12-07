package com.bvyhanoi.feedback.controller;

import com.bvyhanoi.feedback.dto.CreateFeedbackRequest;
import com.bvyhanoi.feedback.dto.FeedbackDTO;
import com.bvyhanoi.feedback.dto.FeedbackHistoryDTO;
import com.bvyhanoi.feedback.dto.PagedResponse;
import com.bvyhanoi.feedback.dto.ProcessFeedbackRequest;
import com.bvyhanoi.feedback.entity.Feedback;
import com.bvyhanoi.feedback.service.AuthService;
import com.bvyhanoi.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/feedbacks")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class FeedbackController {
    
    @Autowired
    private FeedbackService feedbackService;
    
    @Autowired
    private AuthService authService;
    
    @GetMapping
    public ResponseEntity<PagedResponse<FeedbackDTO>> getFeedbacks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Feedback.FeedbackStatus status,
            @RequestParam(required = false) Feedback.FeedbackLevel level,
            @RequestParam(required = false) Feedback.FeedbackChannel channel,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false, defaultValue = "receivedDate,desc") String sort) {
        PagedResponse<FeedbackDTO> response = feedbackService.getAllFeedbacks(
            page, size, keyword, status, level, channel, departmentId, doctorId, dateFrom, dateTo, sort);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<FeedbackDTO>> getMyFeedbacks() {
        Long userId = authService.getCurrentUser().getId();
        List<FeedbackDTO> feedbacks = feedbackService.getMyFeedbacks(userId);
        return ResponseEntity.ok(feedbacks);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackDTO> getFeedbackById(@PathVariable Long id) {
        FeedbackDTO feedback = feedbackService.getFeedbackById(id);
        return ResponseEntity.ok(feedback);
    }
    
    @GetMapping("/by-code/{code}")
    public ResponseEntity<FeedbackDTO> getFeedbackByCode(@PathVariable String code) {
        FeedbackDTO feedback = feedbackService.getFeedbackByCode(code);
        return ResponseEntity.ok(feedback);
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEIVER')")
    public ResponseEntity<FeedbackDTO> createFeedback(@Valid @RequestBody CreateFeedbackRequest request) {
        Long userId = authService.getCurrentUser().getId();
        FeedbackDTO feedback = feedbackService.createFeedback(request, userId);
        return ResponseEntity.ok(feedback);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEIVER')")
    public ResponseEntity<FeedbackDTO> updateFeedback(@PathVariable Long id, @Valid @RequestBody CreateFeedbackRequest request) {
        Long userId = authService.getCurrentUser().getId();
        FeedbackDTO feedback = feedbackService.updateFeedback(id, request, userId);
        return ResponseEntity.ok(feedback);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteFeedback(@PathVariable Long id) {
        feedbackService.deleteFeedback(id);
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public ResponseEntity<FeedbackDTO> assignHandler(@PathVariable Long id, @RequestBody Map<String, Long> request) {
        Long handlerId = request.get("handlerId");
        Long userId = authService.getCurrentUser().getId();
        FeedbackDTO feedback = feedbackService.assignHandler(id, handlerId, userId);
        return ResponseEntity.ok(feedback);
    }
    
    @PutMapping("/{id}/process")
    @PreAuthorize("hasAnyRole('ADMIN', 'LEADER')")
    public ResponseEntity<FeedbackDTO> processFeedback(@PathVariable Long id, @RequestBody ProcessFeedbackRequest request) {
        Long userId = authService.getCurrentUser().getId();
        FeedbackDTO feedback = feedbackService.processFeedback(id, request, userId);
        return ResponseEntity.ok(feedback);
    }
    
    @PutMapping("/{id}/processing")
    @PreAuthorize("hasAnyRole('ADMIN', 'HANDLER')")
    public ResponseEntity<FeedbackDTO> updateProcessing(@PathVariable Long id, @RequestBody ProcessFeedbackRequest request) {
        Long userId = authService.getCurrentUser().getId();
        FeedbackDTO feedback = feedbackService.updateProcessing(id, request, userId);
        return ResponseEntity.ok(feedback);
    }
    
    @GetMapping("/{id}/history")
    public ResponseEntity<List<FeedbackHistoryDTO>> getProcessHistory(@PathVariable Long id) {
        List<FeedbackHistoryDTO> history = feedbackService.getProcessHistory(id);
        return ResponseEntity.ok(history);
    }
}

