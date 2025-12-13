package com.bvyhanoi.feedback.controller;

import com.bvyhanoi.feedback.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AdminController {
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private FeedbackHistoryRepository feedbackHistoryRepository;
    
    /**
     * Delete all demo data except users
     * WARNING: This will DELETE data from the REAL database!
     */
    @DeleteMapping("/data/demo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteDemoData() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Count before deletion
            long historyCount = feedbackHistoryRepository.count();
            long ratingCount = ratingRepository.count();
            long notifCount = notificationRepository.count();
            long feedbackCount = feedbackRepository.count();
            long doctorCount = doctorRepository.count();
            long deptCount = departmentRepository.count();
            
            Map<String, Long> deletedCounts = new HashMap<>();
            deletedCounts.put("feedbackHistory", historyCount);
            deletedCounts.put("ratings", ratingCount);
            deletedCounts.put("notifications", notifCount);
            deletedCounts.put("feedbacks", feedbackCount);
            deletedCounts.put("doctors", doctorCount);
            deletedCounts.put("departments", deptCount);
            
            // Delete in order to respect foreign key constraints
            feedbackHistoryRepository.deleteAll();
            ratingRepository.deleteAll();
            notificationRepository.deleteAll();
            feedbackRepository.deleteAll();
            doctorRepository.deleteAll();
            departmentRepository.deleteAll();
            
            response.put("success", true);
            response.put("message", "Demo data deleted successfully (users preserved)");
            response.put("deletedCounts", deletedCounts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting demo data: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get statistics about current data
     */
    @GetMapping("/data/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getDataStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("departments", departmentRepository.count());
        stats.put("doctors", doctorRepository.count());
        stats.put("feedbacks", feedbackRepository.count());
        stats.put("ratings", ratingRepository.count());
        stats.put("notifications", notificationRepository.count());
        stats.put("feedbackHistory", feedbackHistoryRepository.count());
        
        return ResponseEntity.ok(stats);
    }
}

