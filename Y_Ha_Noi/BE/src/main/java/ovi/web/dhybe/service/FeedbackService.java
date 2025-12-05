package ovi.web.dhybe.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ovi.web.dhybe.dto.auth.UserDto;
import ovi.web.dhybe.dto.common.PageResponse;
import ovi.web.dhybe.dto.feedback.*;
import ovi.web.dhybe.entity.Department;
import ovi.web.dhybe.entity.Doctor;
import ovi.web.dhybe.entity.Feedback;
import ovi.web.dhybe.entity.FeedbackImage;
import ovi.web.dhybe.entity.FeedbackLog;
import ovi.web.dhybe.enums.FeedbackAction;
import ovi.web.dhybe.enums.FeedbackStatus;
import ovi.web.dhybe.mapper.FeedbackMapper;
import ovi.web.dhybe.repository.FeedbackImageRepository;
import ovi.web.dhybe.repository.FeedbackLogRepository;
import ovi.web.dhybe.repository.FeedbackRepository;
import ovi.web.dhybe.repository.UserDtoRepository;
import ovi.web.dhybe.specification.FeedbackSpecifications;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackImageRepository feedbackImageRepository;
    private final FeedbackLogRepository feedbackLogRepository;
    private final DepartmentService departmentService;
    private final DoctorService doctorService;
    private final UserDtoRepository userDtoRepository;
    private final FeedbackMapper feedbackMapper;
    private final NotificationMailService notificationMailService;

    @Transactional
    public FeedbackDetailResponse create(FeedbackCreateRequest request) {
        // Allow FE to omit code/receivedDate/receiverId – generate or infer on server side
        String code = Optional.ofNullable(request.getCode())
                .filter(c -> !c.isBlank())
                .orElseGet(this::generateCode);
        validateUniqueCode(code);

        LocalDateTime receivedDate = Optional.ofNullable(request.getReceivedDate())
                .orElse(LocalDateTime.now());

        Department department = departmentService.getEntity(request.getDepartmentId());
        Doctor doctor = Optional.ofNullable(request.getDoctorId())
                .map(doctorService::getEntity)
                .orElse(null);

        UserDto receiver = Optional.ofNullable(request.getReceiverId())
                .map(this::getUserById)
                .orElseGet(this::getCurrentUser);

        Feedback feedback = Feedback.builder()
                .code(code)
                .receivedDate(receivedDate)
                .channel(request.getChannel())
                .content(request.getContent())
                .department(department)
                .doctor(doctor)
                .level(request.getLevel())
                .receiver(receiver)
                .status(FeedbackStatus.NEW)
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        logAction(saved, receiver, FeedbackAction.CREATED, null, FeedbackStatus.NEW, "Created feedback");
        notificationMailService.sendFeedbackCreated(saved);
        return getDetail(saved.getId());
    }

    @Transactional
    public FeedbackDetailResponse update(Long id,FeedbackUpdateRequest request) {
        Feedback feedback = getEntity(id);

        if (request.getContent() != null) {
            feedback.setContent(request.getContent());
        }
        if (request.getDepartmentId() != null) {
            feedback.setDepartment(departmentService.getEntity(request.getDepartmentId()));
        }
        if (request.getDoctorId() != null) {
            feedback.setDoctor(doctorService.getEntity(request.getDoctorId()));
        }
        if (request.getLevel() != null) {
            feedback.setLevel(request.getLevel());
        }
        if (request.getProcessNote() != null) {
            feedback.setProcessNote(request.getProcessNote());
        }
        if (request.getStatus() != null && request.getStatus() != feedback.getStatus()) {
            logAction(feedback, null, FeedbackAction.STATUS_CHANGE, feedback.getStatus(), request.getStatus(),
                    "Status updated manually");
            feedback.setStatus(request.getStatus());
            if (request.getStatus() == FeedbackStatus.COMPLETED) {
                feedback.setCompletedDate(LocalDateTime.now());
                notificationMailService.sendFeedbackCompleted(feedback);
            }
        }
        return getDetail(feedback.getId());
    }

    @Transactional(readOnly = true)
    public PageResponse<FeedbackListItemResponse> search(FeedbackFilterCriteria criteria, Pageable pageable) {
        Specification<Feedback> spec = FeedbackSpecifications.withFilters(criteria);
        Page<Feedback> page = feedbackRepository.findAll(spec, pageable);
        Page<FeedbackListItemResponse> mapped = page.map(feedbackMapper::toListItem);
        return PageResponse.from(mapped);
    }

    @Transactional(readOnly = true)
    public FeedbackDetailResponse getDetail(Long id) {
        Feedback feedback = getEntity(id);
        List<FeedbackImage> images = feedbackImageRepository.findByFeedback_Id(id);
        List<FeedbackLog> logs = feedbackLogRepository.findByFeedback_IdOrderByCreatedDateAsc(id);
        return feedbackMapper.toDetail(feedback, images, logs);
    }

    @Transactional
    public void delete(Long id) {
        Feedback feedback = getEntity(id);
        feedbackRepository.delete(feedback);
    }

    @Transactional
    public FeedbackDetailResponse assign(Long feedbackId, FeedbackAssignRequest request, Long actorId) {
        Feedback feedback = getEntity(feedbackId);
        FeedbackStatus oldStatus = feedback.getStatus();
        UserDto handler = getUserById(request.getHandlerId());
        feedback.setHandler(handler);
        feedback.setStatus(FeedbackStatus.ASSIGNED);
        feedback.setAssignedDate(LocalDateTime.now());
        UserDto actor = actorId != null ? getUserById(actorId) : null;
        logAction(feedback, actor, FeedbackAction.ASSIGNED, oldStatus, FeedbackStatus.ASSIGNED,
                Optional.ofNullable(request.getNote()).orElse("Assigned to handler"));
        notificationMailService.sendFeedbackAssigned(feedback);
        return getDetail(feedbackId);
    }

    @Transactional
    public FeedbackDetailResponse process(Long feedbackId, FeedbackProcessingRequest request, Long actorId) {
        Feedback feedback = getEntity(feedbackId);
        FeedbackStatus previousStatus = feedback.getStatus();
        feedback.setStatus(request.getStatus());
        feedback.setProcessNote(request.getNote());
        feedback.setLastProcessDate(LocalDateTime.now());
        feedback.setProcessCount(Optional.ofNullable(feedback.getProcessCount()).orElse(0) + 1);
        if (request.getStatus() == FeedbackStatus.COMPLETED) {
            feedback.setCompletedDate(LocalDateTime.now());
        }

        if (request.getImageIds() != null && !request.getImageIds().isEmpty()) {
            List<FeedbackImage> images = feedbackImageRepository.findAllById(request.getImageIds());
            boolean allMatch = images.stream().allMatch(image -> image.getFeedback().getId().equals(feedbackId));
            if (!allMatch) {
                throw new IllegalArgumentException("Some images do not belong to this feedback");
            }
        }

        UserDto actor = actorId != null ? getUserById(actorId) : null;
        logAction(feedback, actor, FeedbackAction.PROCESS_UPDATE, previousStatus, request.getStatus(), request.getNote());
        if (request.getStatus() == FeedbackStatus.COMPLETED) {
            logAction(feedback, actor, FeedbackAction.COMPLETED, previousStatus, FeedbackStatus.COMPLETED,
                    "Marked as completed");
            notificationMailService.sendFeedbackCompleted(feedback);
        }
        return getDetail(feedbackId);
    }

    @Transactional(readOnly = true)
    public List<FeedbackHistoryItem> history(Long feedbackId) {
        List<FeedbackLog> logs = feedbackLogRepository.findByFeedback_IdOrderByCreatedDateAsc(feedbackId);
        return logs.stream()
                .map(feedbackMapper::toHistoryItem)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Feedback> listCompletedWithoutRating() {
        return feedbackRepository.findByStatus(FeedbackStatus.COMPLETED).stream()
                .filter(feedback -> feedback.getRating() == null)
                .collect(Collectors.toList());
    }

    public Feedback getEntity(Long id) {
        return feedbackRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Feedback not found: " + id));
    }

    private String generateCode() {
        // Simple code generator: PA-YYYYMMDD-HHMMSS-random
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.toString()
                .replace("T", "")
                .replace(":", "")
                .replace("-", "")
                .substring(0, 14);
        return "PA-" + timestamp;
    }

    private void validateUniqueCode(String code) {
        if (feedbackRepository.existsByCode(code)) {
            throw new IllegalArgumentException("Feedback code already exists: " + code);
        }
    }

    private UserDto getUserById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("User id is required");
        }
        return userDtoRepository.findById(Math.toIntExact(id))
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    private UserDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new IllegalArgumentException("No authenticated user found");
        }
        return userDtoRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + authentication.getName()));
    }

    private void logAction(Feedback feedback,
                           UserDto actor,
                           FeedbackAction action,
                           FeedbackStatus oldStatus,
                           FeedbackStatus newStatus,
                           String note) {
        FeedbackLog log = FeedbackLog.builder()
                .feedback(feedback)
                .user(actor != null ? actor : feedback.getReceiver())
                .action(action)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .note(note)
                .build();
        feedbackLogRepository.save(log);
    }
}


