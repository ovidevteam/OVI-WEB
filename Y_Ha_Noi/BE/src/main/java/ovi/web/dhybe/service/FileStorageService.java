package ovi.web.dhybe.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ovi.web.dhybe.dto.upload.FileUploadResponse;
import ovi.web.dhybe.entity.Feedback;
import ovi.web.dhybe.entity.FeedbackImage;
import ovi.web.dhybe.dto.auth.UserDto;
import ovi.web.dhybe.enums.FeedbackImageType;
import ovi.web.dhybe.repository.FeedbackImageRepository;
import ovi.web.dhybe.repository.FeedbackRepository;
import ovi.web.dhybe.repository.UserDtoRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackImageRepository feedbackImageRepository;
    private final UserDtoRepository userDtoRepository;

    @Value("${app.storage.base-dir:uploads}")
    private String baseDir;

    @Value("${app.storage.public-base-url:/uploads}")
    private String publicBaseUrl;

    @Transactional
    public FileUploadResponse store(Long feedbackId,
                                    FeedbackImageType type,
                                    MultipartFile file,
                                    Long uploaderId) throws IOException {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new EntityNotFoundException("Feedback not found: " + feedbackId));
        UserDto uploader = userDtoRepository.findById(Math.toIntExact(uploaderId))
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + uploaderId));

        String cleanName = StringUtils.cleanPath(file.getOriginalFilename());
        String ext = "";
        int dot = cleanName.lastIndexOf('.');
        if (dot > -1) {
            ext = cleanName.substring(dot);
        }
        String newName = UUID.randomUUID() + ext;
        Path storageDir = Paths.get(baseDir, type.name().toLowerCase());
        Files.createDirectories(storageDir);
        Path destination = storageDir.resolve(newName);
        file.transferTo(destination);

        FeedbackImage image = FeedbackImage.builder()
                .feedback(feedback)
                .imagePath(destination.toString())
                .imageType(type)
                .uploadedBy(uploader)
                .uploadedDate(LocalDateTime.now())
                .build();
        FeedbackImage saved = feedbackImageRepository.save(image);

        String url = publicBaseUrl + "/" + type.name().toLowerCase() + "/" + newName;
        return FileUploadResponse.builder()
                .id(saved.getId())
                .fileName(cleanName)
                .url(url)
                .imageType(type)
                .build();
    }

    @Transactional(readOnly = true)
    public Resource load(Long imageId) {
        FeedbackImage image = feedbackImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found: " + imageId));
        return new FileSystemResource(image.getImagePath());
    }

    @Transactional
    public void delete(Long imageId) throws IOException {
        FeedbackImage image = feedbackImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found: " + imageId));
        Path path = Paths.get(image.getImagePath());
        if (Files.exists(path)) {
            Files.delete(path);
        }
        feedbackImageRepository.delete(image);
    }
}





