package com.bvyhanoi.feedback.service;

import com.bvyhanoi.feedback.dto.FeedbackImageDTO;
import com.bvyhanoi.feedback.entity.FeedbackImage;
import com.bvyhanoi.feedback.exception.ResourceNotFoundException;
import com.bvyhanoi.feedback.repository.FeedbackImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UploadService {
    
    @Value("${file.image-dir:./uploads/images}")
    private String imageDir;
    
    @Autowired
    private FeedbackImageRepository feedbackImageRepository;
    
    @Transactional
    public List<FeedbackImageDTO> uploadFeedbackImages(Long feedbackId, List<MultipartFile> files) throws IOException {
        return uploadImages(feedbackId, files, FeedbackImage.ImageType.FEEDBACK);
    }
    
    @Transactional
    public List<FeedbackImageDTO> uploadProcessImages(Long feedbackId, List<MultipartFile> files) throws IOException {
        return uploadImages(feedbackId, files, FeedbackImage.ImageType.PROCESS);
    }
    
    private List<FeedbackImageDTO> uploadImages(Long feedbackId, List<MultipartFile> files, FeedbackImage.ImageType imageType) throws IOException {
        Path uploadPath = Paths.get(imageDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        List<FeedbackImageDTO> imageDTOs = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            FeedbackImage image = new FeedbackImage();
            image.setFeedbackId(feedbackId);
            image.setFilename(filename);
            image.setFilePath(filePath.toString());
            image.setImageType(imageType);
            image = feedbackImageRepository.save(image);
            
            FeedbackImageDTO dto = new FeedbackImageDTO();
            dto.setId(image.getId());
            dto.setFilename(image.getFilename());
            dto.setUrl("/api/upload/images/" + image.getFilename());
            dto.setImageType(image.getImageType().name());
            imageDTOs.add(dto);
        }
        
        return imageDTOs;
    }
    
    @Transactional
    public void deleteImage(Long imageId) throws IOException {
        FeedbackImage image = feedbackImageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image", imageId));
        
        Path filePath = Paths.get(image.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        feedbackImageRepository.delete(image);
    }
    
    public byte[] getImage(String filename) throws IOException {
        Path filePath = Paths.get(imageDir).resolve(filename);
        if (!Files.exists(filePath)) {
            throw new ResourceNotFoundException("Image file not found: " + filename);
        }
        return Files.readAllBytes(filePath);
    }
}

