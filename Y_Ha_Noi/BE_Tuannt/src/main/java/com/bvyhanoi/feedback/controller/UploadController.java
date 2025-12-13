package com.bvyhanoi.feedback.controller;

import com.bvyhanoi.feedback.dto.FeedbackImageDTO;
import com.bvyhanoi.feedback.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class UploadController {
    
    @Autowired
    private UploadService uploadService;
    
    @PostMapping("/feedback-images")
    public ResponseEntity<Map<String, List<FeedbackImageDTO>>> uploadFeedbackImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("feedbackId") Long feedbackId) throws IOException {
        List<FeedbackImageDTO> images = uploadService.uploadFeedbackImages(feedbackId, files);
        Map<String, List<FeedbackImageDTO>> response = new HashMap<>();
        response.put("images", images);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/process-images")
    public ResponseEntity<Map<String, List<FeedbackImageDTO>>> uploadProcessImages(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("feedbackId") Long feedbackId) throws IOException {
        List<FeedbackImageDTO> images = uploadService.uploadProcessImages(feedbackId, files);
        Map<String, List<FeedbackImageDTO>> response = new HashMap<>();
        response.put("images", images);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Map<String, String>> deleteImage(@PathVariable Long imageId) throws IOException {
        uploadService.deleteImage(imageId);
        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/images/{filename}")
    public ResponseEntity<byte[]> getImage(@PathVariable String filename) throws IOException {
        byte[] imageData = uploadService.getImage(filename);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return ResponseEntity.ok().headers(headers).body(imageData);
    }
}

