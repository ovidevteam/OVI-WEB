package ovi.web.dhybe.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ovi.web.dhybe.dto.common.ApiResponse;
import ovi.web.dhybe.dto.upload.FileUploadResponse;
import ovi.web.dhybe.enums.FeedbackImageType;
import ovi.web.dhybe.service.FileStorageService;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping(value = "/feedback-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','RECEIVER')")
    public FileUploadResponse uploadFeedbackImage(@RequestParam Long feedbackId,
                                                  @RequestParam MultipartFile file,
                                                  @RequestParam Long uploadedBy) throws Exception {
        return fileStorageService.store(feedbackId, FeedbackImageType.FEEDBACK, file, uploadedBy);
    }

    @PostMapping(value = "/process-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','HANDLER')")
    public FileUploadResponse uploadProcessImage(@RequestParam Long feedbackId,
                                                 @RequestParam MultipartFile file,
                                                 @RequestParam Long uploadedBy) throws Exception {
        return fileStorageService.store(feedbackId, FeedbackImageType.PROCESS, file, uploadedBy);
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        Resource resource = fileStorageService.load(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/images/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) throws Exception {
        fileStorageService.delete(id);
        return ApiResponse.ok("Image deleted");
    }
}

