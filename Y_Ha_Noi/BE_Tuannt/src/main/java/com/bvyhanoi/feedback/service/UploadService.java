package com.bvyhanoi.feedback.service;

import com.bvyhanoi.feedback.dto.FeedbackImageDTO;
import com.bvyhanoi.feedback.entity.FeedbackImage;
import com.bvyhanoi.feedback.exception.ResourceNotFoundException;
import com.bvyhanoi.feedback.repository.FeedbackImageRepository;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class UploadService {
    
    @Value("${file.image-dir:./uploads/images}")
    private String imageDir;
    
    @Value("${minio.bucket-name:feedback-images}")
    private String bucketName;
    
    @Autowired
    private FeedbackImageRepository feedbackImageRepository;
    
    @Autowired
    private MinioClient minioClient;
    
    @Transactional
    public List<FeedbackImageDTO> uploadFeedbackImages(Long feedbackId, List<MultipartFile> files) throws IOException {
        return uploadImages(feedbackId, files, FeedbackImage.ImageType.FEEDBACK);
    }
    
    @Transactional
    public List<FeedbackImageDTO> uploadProcessImages(Long feedbackId, List<MultipartFile> files) throws IOException {
        return uploadImages(feedbackId, files, FeedbackImage.ImageType.PROCESS);
    }
    
    private List<FeedbackImageDTO> uploadImages(Long feedbackId, List<MultipartFile> files, FeedbackImage.ImageType imageType) throws IOException {
        // Tạo thư mục local nếu chưa tồn tại
        Path uploadPath = Paths.get(imageDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Đảm bảo bucket MinIO tồn tại
        ensureBucketExists();
        
        List<FeedbackImageDTO> imageDTOs = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".") 
                ? originalFilename.substring(originalFilename.lastIndexOf(".")) 
                : "";
            String filename = UUID.randomUUID().toString() + extension;
            
            // Đọc file vào byte array một lần để dùng cho cả local và MinIO
            byte[] fileBytes = file.getBytes();
            
            // Upload lên local storage
            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, fileBytes);
            
            // Upload lên MinIO
            uploadToMinIO(filename, fileBytes, file.getContentType());
            
            // Lưu metadata vào database
            FeedbackImage image = new FeedbackImage();
            image.setFeedbackId(feedbackId);
            image.setFilename(filename);
            image.setFilePath(filePath.toString()); // Lưu local path
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
    
    private void ensureBucketExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error checking/creating MinIO bucket: " + e.getMessage(), e);
        }
    }
    
    private void uploadToMinIO(String filename, byte[] fileBytes, String contentType) throws IOException {
        try {
            // Upload lên MinIO sử dụng byte array
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .stream(new java.io.ByteArrayInputStream(fileBytes), fileBytes.length, -1)
                    .contentType(contentType)
                    .build()
            );
        } catch (Exception e) {
            throw new IOException("Error uploading file to MinIO: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public void deleteImage(Long imageId) throws IOException {
        FeedbackImage image = feedbackImageRepository.findById(imageId)
            .orElseThrow(() -> new ResourceNotFoundException("Image", imageId));
        
        // Xóa file local
        Path filePath = Paths.get(image.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        // Xóa file trên MinIO
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(image.getFilename())
                    .build()
            );
        } catch (Exception e) {
            // Log error nhưng không throw để không ảnh hưởng đến việc xóa local
            System.err.println("Error deleting file from MinIO: " + e.getMessage());
        }
        
        feedbackImageRepository.delete(image);
    }
    
    public byte[] getImage(String filename) throws IOException {
        // Ưu tiên lấy từ local, nếu không có thì lấy từ MinIO
        Path filePath = Paths.get(imageDir).resolve(filename);
        if (Files.exists(filePath)) {
            return Files.readAllBytes(filePath);
        }
        
        // Nếu không có trong local, lấy từ MinIO
        try {
            InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(filename)
                    .build()
            );
            return stream.readAllBytes();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Image file not found: " + filename);
        }
    }
}

