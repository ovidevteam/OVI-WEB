package com.bvyhanoi.feedback.service;

import com.bvyhanoi.feedback.dto.CreateFeedbackRequest;
import com.bvyhanoi.feedback.dto.FeedbackDTO;
import com.bvyhanoi.feedback.dto.FeedbackHistoryDTO;
import com.bvyhanoi.feedback.dto.FeedbackImageDTO;
import com.bvyhanoi.feedback.dto.PagedResponse;
import com.bvyhanoi.feedback.dto.ProcessFeedbackRequest;
import com.bvyhanoi.feedback.entity.Feedback;
import com.bvyhanoi.feedback.entity.FeedbackHistory;
import com.bvyhanoi.feedback.entity.FeedbackImage;
import com.bvyhanoi.feedback.entity.User;
import com.bvyhanoi.feedback.exception.ResourceNotFoundException;
import com.bvyhanoi.feedback.exception.UnauthorizedException;
import com.bvyhanoi.feedback.repository.DepartmentRepository;
import com.bvyhanoi.feedback.repository.DoctorRepository;
import com.bvyhanoi.feedback.repository.FeedbackHistoryRepository;
import com.bvyhanoi.feedback.repository.FeedbackImageRepository;
import com.bvyhanoi.feedback.repository.FeedbackRepository;
import com.bvyhanoi.feedback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private FeedbackImageRepository feedbackImageRepository;
    
    @Autowired
    private FeedbackHistoryRepository feedbackHistoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    public PagedResponse<FeedbackDTO> getAllFeedbacks(int page, int size, String keyword, 
                                                       Feedback.FeedbackStatus status, 
                                                       Feedback.FeedbackLevel level,
                                                       Feedback.FeedbackChannel channel,
                                                       Long departmentId,
                                                       Long doctorId,
                                                       LocalDate dateFrom,
                                                       LocalDate dateTo,
                                                       String sort) {
        Pageable pageable = PageRequest.of(page - 1, size);
        
        // Prepare keyword pattern for LIKE query - use empty string instead of null to avoid SQL issues
        String keywordPattern = keyword != null && !keyword.trim().isEmpty() ? "%" + keyword.toLowerCase() + "%" : null;
        String statusStr = status != null ? status.name() : null;
        String levelStr = level != null ? level.name() : null;
        String channelStr = channel != null ? channel.name() : null;
        // Convert LocalDate to String for native query to avoid NULL type issues
        String dateFromStr = dateFrom != null ? dateFrom.toString() : null;
        String dateToStr = dateTo != null ? dateTo.toString() : null;
        // Use separate query methods to avoid NULL handling issues in native query
        Page<Feedback> feedbackPage;
        if (doctorId != null) {
            feedbackPage = feedbackRepository.searchWithDoctor(statusStr, levelStr, channelStr, departmentId, doctorId, dateFromStr, dateToStr, keyword, keywordPattern, pageable);
        } else {
            feedbackPage = feedbackRepository.search(statusStr, levelStr, channelStr, departmentId, dateFromStr, dateToStr, keyword, keywordPattern, pageable);
        }
        
        List<FeedbackDTO> feedbackDTOs = feedbackPage.getContent().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        
        return PagedResponse.of(feedbackDTOs, feedbackPage.getTotalElements(), page, size);
    }
    
    public List<FeedbackDTO> getMyFeedbacks(Long handlerId) {
        List<Feedback> feedbacks = feedbackRepository.findByHandlerId(handlerId);
        return feedbacks.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    public FeedbackDTO getFeedbackById(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback", id));
        return toDTO(feedback);
    }
    
    public FeedbackDTO getFeedbackByCode(String code) {
        Feedback feedback = feedbackRepository.findByCode(code)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback with code: " + code));
        return toDTO(feedback);
    }
    
    @Transactional
    public FeedbackDTO createFeedback(CreateFeedbackRequest request, Long userId) {
        String code = generateFeedbackCode();
        
        Feedback feedback = new Feedback();
        feedback.setCode(code);
        feedback.setContent(request.getContent());
        feedback.setChannel(request.getChannel());
        feedback.setLevel(request.getLevel());
        feedback.setDepartmentId(request.getDepartmentId());
        feedback.setDoctorId(request.getDoctorId());
        feedback.setReceiverId(userId); // Set receiver as the user who creates the feedback
        feedback.setStatus(Feedback.FeedbackStatus.NEW);
        feedback.setReceivedDate(LocalDate.now());
        
        // Auto-assign handler from department if available (but status remains NEW)
        Long handlerId = null;
        if (request.getDepartmentId() != null) {
            handlerId = departmentRepository.findById(request.getDepartmentId())
                .map(dept -> dept.getHandlerId())
                .orElse(null);
            
            if (handlerId != null) {
                feedback.setHandlerId(handlerId);
                // Status remains NEW even if handler is assigned
            }
        }
        
        feedback = feedbackRepository.save(feedback);
        
        // Create history - always NEW status when creating
        createHistory(feedback.getId(), Feedback.FeedbackStatus.NEW, "Phản ánh mới được tạo", userId);
        
        // Create notification for ADMIN and LEADER
        notificationService.createFeedbackNotification(feedback.getId(), "Phản ánh mới: " + code);
        
        return toDTO(feedback);
    }
    
    @Transactional
    public FeedbackDTO updateFeedback(Long id, CreateFeedbackRequest request, Long userId) {
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback", id));
        
        // Track what changed for history
        StringBuilder changes = new StringBuilder();
        if (!feedback.getContent().equals(request.getContent())) {
            changes.append("Cập nhật nội dung. ");
        }
        if (feedback.getChannel() != request.getChannel()) {
            changes.append("Thay đổi kênh tiếp nhận. ");
        }
        if (feedback.getLevel() != request.getLevel()) {
            changes.append("Thay đổi mức độ. ");
        }
        if (feedback.getDepartmentId() != null && !feedback.getDepartmentId().equals(request.getDepartmentId())) {
            changes.append("Thay đổi phòng ban. ");
        } else if (feedback.getDepartmentId() == null && request.getDepartmentId() != null) {
            changes.append("Thêm phòng ban. ");
        }
        if (feedback.getDoctorId() != null && !feedback.getDoctorId().equals(request.getDoctorId())) {
            changes.append("Thay đổi bác sĩ. ");
        } else if (feedback.getDoctorId() == null && request.getDoctorId() != null) {
            changes.append("Thêm bác sĩ. ");
        }
        
        feedback.setContent(request.getContent());
        feedback.setChannel(request.getChannel());
        feedback.setLevel(request.getLevel());
        feedback.setDepartmentId(request.getDepartmentId());
        feedback.setDoctorId(request.getDoctorId());
        
        feedback = feedbackRepository.save(feedback);
        
        // Create history if there were changes
        if (changes.length() > 0) {
            String note = changes.toString().trim();
            createHistory(id, feedback.getStatus(), note, userId);
        }
        
        return toDTO(feedback);
    }
    
    @Transactional
    public void deleteFeedback(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback", id));
        feedbackRepository.delete(feedback);
    }
    
    @Transactional
    public FeedbackDTO assignHandler(Long id, Long handlerId, Long userId) {
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback", id));
        
        User handler = userRepository.findById(handlerId)
            .orElseThrow(() -> new ResourceNotFoundException("User", handlerId));
        
        if (handler.getRole() != User.Role.HANDLER) {
            throw new RuntimeException("User is not a handler");
        }
        
        feedback.setHandlerId(handlerId);
        feedback.setStatus(Feedback.FeedbackStatus.ASSIGNED);
        feedback = feedbackRepository.save(feedback);
        
        createHistory(id, Feedback.FeedbackStatus.ASSIGNED, "Được phân công cho " + handler.getFullName(), userId);
        notificationService.createAssignedNotification(handlerId, feedback.getId(), feedback.getCode());
        
        return toDTO(feedback);
    }
    
    @Transactional
    public FeedbackDTO processFeedback(Long id, ProcessFeedbackRequest request, Long userId) {
        Feedback feedback = feedbackRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Feedback", id));
        
        if (feedback.getHandlerId() != null && !feedback.getHandlerId().equals(userId)) {
            throw new UnauthorizedException("You are not assigned to this feedback");
        }
        
        if (request.getStatus() != null) {
            feedback.setStatus(request.getStatus());
            if (request.getStatus() == Feedback.FeedbackStatus.COMPLETED) {
                feedback.setCompletedDate(LocalDate.now());
            }
        }
        
        feedback = feedbackRepository.save(feedback);
        
        // Use content from request, fallback to note, or default message
        String content = request.getContent();
        if (content == null || content.trim().isEmpty()) {
            // Fallback to result (for backward compatibility) or note
            content = request.getResult() != null ? request.getResult() : 
                     (request.getNote() != null ? request.getNote() : "Cập nhật trạng thái");
        }
        String note = request.getNote();
        List<Long> imageIds = request.getImageIds() != null ? request.getImageIds() : new ArrayList<>();
        createHistory(id, feedback.getStatus(), content, note, userId, imageIds);
        
        if (feedback.getStatus() == Feedback.FeedbackStatus.COMPLETED) {
            notificationService.createCompletedNotification(feedback.getId(), feedback.getCode());
        }
        
        return toDTO(feedback);
    }
    
    @Transactional
    public FeedbackDTO updateProcessing(Long id, ProcessFeedbackRequest request, Long userId) {
        return processFeedback(id, request, userId);
    }
    
    public List<FeedbackHistoryDTO> getProcessHistory(Long feedbackId) {
        List<FeedbackHistory> histories = feedbackHistoryRepository.findByFeedbackIdOrderByCreatedAtDesc(feedbackId);
        return histories.stream().map(history -> {
            FeedbackHistoryDTO dto = new FeedbackHistoryDTO();
            dto.setId(history.getId());
            dto.setFeedbackId(history.getFeedbackId());
            dto.setStatus(history.getStatus());
            dto.setContent(history.getContent());
            dto.setNote(history.getNote());
            dto.setCreatedBy(history.getCreatedBy());
            dto.setCreatedAt(history.getCreatedAt());
            
            // Load user name
            userRepository.findById(history.getCreatedBy())
                .ifPresent(user -> dto.setCreatedByName(user.getFullName()));
            
            // Parse imageIds from JSON string
            if (history.getImageIds() != null && !history.getImageIds().isEmpty()) {
                try {
                    List<Long> imageIds = objectMapper.readValue(history.getImageIds(), new TypeReference<List<Long>>() {});
                    dto.setImageIds(imageIds);
                    
                    // Load image details
                    if (imageIds != null && !imageIds.isEmpty()) {
                        List<FeedbackImageDTO> images = imageIds.stream()
                            .map(imageId -> {
                                return feedbackImageRepository.findById(imageId)
                                    .map(img -> {
                                        FeedbackImageDTO imgDTO = new FeedbackImageDTO();
                                        imgDTO.setId(img.getId());
                                        imgDTO.setFilename(img.getFilename());
                                        imgDTO.setUrl("/api/upload/images/" + img.getFilename());
                                        imgDTO.setImageType(img.getImageType().name());
                                        return imgDTO;
                                    })
                                    .orElse(null);
                            })
                            .filter(img -> img != null)
                            .collect(Collectors.toList());
                        dto.setImages(images);
                    }
                } catch (Exception e) {
                    // If JSON parsing fails, set empty list
                    dto.setImageIds(new ArrayList<>());
                }
            } else {
                dto.setImageIds(new ArrayList<>());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    private String generateFeedbackCode() {
        String dateStr = LocalDate.now().format(CODE_FORMATTER);
        String baseCode = "PA-" + dateStr + "-";
        String datePattern = "PA-" + dateStr + "-%";
        
        // Find all codes for today and extract max sequence
        List<String> codes = feedbackRepository.findCodesByDatePattern(datePattern);
        int maxSequence = 0;
        
        for (String code : codes) {
            try {
                // Extract sequence number from code (format: PA-yyyyMMdd-XXX)
                String sequenceStr = code.substring(baseCode.length());
                int sequence = Integer.parseInt(sequenceStr);
                if (sequence > maxSequence) {
                    maxSequence = sequence;
                }
            } catch (Exception e) {
                // Ignore invalid codes
            }
        }
        
        // Generate next sequence
        int nextSequence = maxSequence + 1;
        String code = baseCode + String.format("%03d", nextSequence);
        
        // Double-check if code exists (for concurrent requests)
        int attempts = 0;
        while (feedbackRepository.existsByCode(code) && attempts < 100) {
            nextSequence++;
            code = baseCode + String.format("%03d", nextSequence);
            attempts++;
        }
        
        // Fallback: use timestamp if still exists (very rare case)
        if (feedbackRepository.existsByCode(code)) {
            String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
            code = baseCode + timestamp;
        }
        
        return code;
    }
    
    private void createHistory(Long feedbackId, Feedback.FeedbackStatus status, String note, Long userId) {
        createHistory(feedbackId, status, note, null, userId, null);
    }
    
    private void createHistory(Long feedbackId, Feedback.FeedbackStatus status, String content, String note, Long userId, List<Long> imageIds) {
        FeedbackHistory history = new FeedbackHistory();
        history.setFeedbackId(feedbackId);
        history.setStatus(status);
        history.setContent(content);
        history.setNote(note);
        history.setCreatedBy(userId);
        
        // Store imageIds as JSON string
        if (imageIds != null && !imageIds.isEmpty()) {
            try {
                history.setImageIds(objectMapper.writeValueAsString(imageIds));
            } catch (Exception e) {
                // If JSON serialization fails, ignore imageIds
            }
        }
        
        feedbackHistoryRepository.save(history);
    }
    
    private FeedbackDTO toDTO(Feedback feedback) {
        FeedbackDTO dto = new FeedbackDTO();
        dto.setId(feedback.getId());
        dto.setCode(feedback.getCode());
        dto.setContent(feedback.getContent());
        dto.setChannel(feedback.getChannel());
        dto.setLevel(feedback.getLevel());
        dto.setStatus(feedback.getStatus());
        dto.setDepartmentId(feedback.getDepartmentId());
        dto.setDoctorId(feedback.getDoctorId());
        dto.setHandlerId(feedback.getHandlerId());
        dto.setReceivedDate(feedback.getReceivedDate());
        dto.setCompletedDate(feedback.getCompletedDate());
        dto.setCreatedAt(feedback.getCreatedAt());
        dto.setUpdatedAt(feedback.getUpdatedAt());
        
        // Load department name
        if (feedback.getDepartmentId() != null) {
            departmentRepository.findById(feedback.getDepartmentId())
                .ifPresent(dept -> dto.setDepartmentName(dept.getName()));
        }
        
        // Load doctor name
        if (feedback.getDoctorId() != null) {
            doctorRepository.findById(feedback.getDoctorId())
                .ifPresent(doctor -> dto.setDoctorName(doctor.getFullName()));
        }
        
        // Load handler name
        if (feedback.getHandlerId() != null) {
            userRepository.findById(feedback.getHandlerId())
                .ifPresent(handler -> dto.setHandlerName(handler.getFullName()));
        }
        
        // Load receiver name
        if (feedback.getReceiverId() != null) {
            dto.setReceiverId(feedback.getReceiverId());
            userRepository.findById(feedback.getReceiverId())
                .ifPresent(receiver -> dto.setReceiverName(receiver.getFullName()));
        }
        
        // Load images
        List<FeedbackImage> images = feedbackImageRepository.findByFeedbackId(feedback.getId());
        dto.setImages(images.stream().map(img -> {
            FeedbackImageDTO imgDTO = new FeedbackImageDTO();
            imgDTO.setId(img.getId());
            imgDTO.setFilename(img.getFilename());
            imgDTO.setUrl("/api/upload/images/" + img.getFilename());
            imgDTO.setImageType(img.getImageType().name());
            return imgDTO;
        }).collect(Collectors.toList()));
        
        return dto;
    }
}

