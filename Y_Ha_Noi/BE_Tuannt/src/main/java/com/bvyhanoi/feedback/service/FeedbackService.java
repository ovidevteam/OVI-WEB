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
import org.springframework.beans.factory.annotation.Value;
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

    @Autowired
    private MailService mailService;

    @Value("${app.mail.portal-url:http://localhost:8080}")
    private String portalUrl;
    
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

        // Gửi mail cho đại diện phòng ban và handler mặc định (nếu có email)
        sendMailOnCreate(feedback, handlerId);
        
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
        feedback.setStatus(Feedback.FeedbackStatus.NEW);
        feedback = feedbackRepository.save(feedback);
        
        createHistory(id, Feedback.FeedbackStatus.NEW, "Được phân công cho " + handler.getFullName(), userId);
        notificationService.createAssignedNotification(handlerId, feedback.getId(), feedback.getCode());
        
        // Gửi email thông báo cho handler (nếu có email)
        if (handler.getEmail() != null && !handler.getEmail().isBlank()) {
            String subject = "Bạn được phân công xử lý phản ánh " + feedback.getCode();
            String deptName = null;
            if (feedback.getDepartmentId() != null) {
                deptName = departmentRepository.findById(feedback.getDepartmentId())
                        .map(d -> d.getName())
                        .orElse(null);
            }
            String body = """
                    Xin chào %s,

                    Bạn vừa được giao xử lý phản ánh: %s
                    Mức độ: %s
                    Phòng ban: %s
                    Nội dung: %s

                    Vui lòng đăng nhập hệ thống để xử lý: %s
                    """.formatted(
                    handler.getFullName(),
                    feedback.getCode(),
                    feedback.getLevel(),
                    deptName != null ? deptName : "Chưa xác định",
                    feedback.getContent(),
                    portalUrl);
            mailService.sendPlainText(handler.getEmail(), subject, body);
        }
        
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
            sendMailOnCompleted(feedback);
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

    private void sendMailOnCreate(Feedback feedback, Long handlerId) {
        // Lấy email phòng ban
        final String[] deptNameHolder = {null};
        String deptEmail = null;
        if (feedback.getDepartmentId() != null) {
            deptEmail = departmentRepository.findById(feedback.getDepartmentId())
                    .map(d -> {
                        deptNameHolder[0] = d.getName();
                        return d.getNotificationEmail();
                    })
                    .orElse(null);
        }

        // Lấy email handler mặc định nếu có
        String handlerEmail = null;
        if (handlerId != null) {
            handlerEmail = userRepository.findById(handlerId)
                    .map(u -> u.getEmail())
                    .orElse(null);
        }

        String subject = "Phản ánh mới " + feedback.getCode();
        String body = """
                Phản ánh mới: %s
                Mức độ: %s
                Phòng ban: %s
                Nội dung: %s

                Đăng nhập để xử lý: %s
                """.formatted(
                feedback.getCode(),
                feedback.getLevel(),
                deptNameHolder[0] != null ? deptNameHolder[0] : "Chưa xác định",
                feedback.getContent(),
                portalUrl
        );

        if (deptEmail != null && !deptEmail.isBlank()) {
            mailService.sendPlainText(deptEmail, subject, body);
        }
        if (handlerEmail != null && !handlerEmail.isBlank()) {
            mailService.sendPlainText(handlerEmail, subject, body);
        }
    }

    private void sendMailOnCompleted(Feedback feedback) {
        // Receiver
        if (feedback.getReceiverId() != null) {
            userRepository.findById(feedback.getReceiverId())
                    .filter(u -> u.getEmail() != null && !u.getEmail().isBlank())
                    .ifPresent(u -> {
                        String subject = "Phản ánh " + feedback.getCode() + " đã hoàn thành";
                        String body = """
                                Phản ánh %s đã được hoàn thành.
                                Trạng thái: %s
                                Nội dung: %s

                                Đăng nhập để xem chi tiết: %s
                                """.formatted(
                                feedback.getCode(),
                                feedback.getStatus(),
                                feedback.getContent(),
                                portalUrl
                        );
                        mailService.sendPlainText(u.getEmail(), subject, body);
                    });
        }

        // Department
        if (feedback.getDepartmentId() != null) {
            departmentRepository.findById(feedback.getDepartmentId())
                    .map(d -> new String[]{d.getNotificationEmail(), d.getName()})
                    .filter(arr -> arr[0] != null && !arr[0].isBlank())
                    .ifPresent(arr -> {
                        String subject = "Phản ánh " + feedback.getCode() + " đã hoàn thành";
                        String body = """
                                Phản ánh %s của khoa %s đã được hoàn thành.
                                Trạng thái: %s
                                Nội dung: %s

                                Đăng nhập để xem chi tiết: %s
                                """.formatted(
                                feedback.getCode(),
                                arr[1] != null ? arr[1] : "Chưa xác định",
                                feedback.getStatus(),
                                feedback.getContent(),
                                portalUrl
                        );
                        mailService.sendPlainText(arr[0], subject, body);
                    });
        }
    }
}

