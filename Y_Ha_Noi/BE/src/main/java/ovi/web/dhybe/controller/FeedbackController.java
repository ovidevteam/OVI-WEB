package ovi.web.dhybe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ovi.web.dhybe.dto.auth.UserDto;
import ovi.web.dhybe.dto.common.ApiResponse;
import ovi.web.dhybe.dto.common.PageResponse;
import ovi.web.dhybe.dto.feedback.*;
import ovi.web.dhybe.dto.feedback.FeedbackFilterCriteria;
import ovi.web.dhybe.dto.feedback.FeedbackProcessingRequest;
import ovi.web.dhybe.dto.feedback.FeedbackAssignRequest;
import ovi.web.dhybe.dto.feedback.FeedbackUpdateRequest;
import ovi.web.dhybe.enums.FeedbackLevel;
import ovi.web.dhybe.enums.FeedbackStatus;
import ovi.web.dhybe.repository.UserDtoRepository;
import ovi.web.dhybe.service.FeedbackService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserDtoRepository userDtoRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','LEADER','RECEIVER','HANDLER','VIEWER')")
    public PageResponse<FeedbackListItemResponse> search(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) FeedbackStatus status,
            @RequestParam(required = false) FeedbackLevel level,
            @RequestParam(required = false) Long handlerId,
            @RequestParam(required = false) Long receiverId,
            @RequestParam(required = false) Boolean rated,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        FeedbackFilterCriteria criteria = new FeedbackFilterCriteria();
        criteria.setDepartmentId(departmentId);
        criteria.setDoctorId(doctorId);
        criteria.setStatus(status);
        criteria.setLevel(level);
        criteria.setHandlerId(handlerId);
        criteria.setReceiverId(receiverId);
        criteria.setRatedOnly(rated);
        criteria.setKeyword(keyword);
        if (fromDate != null) {
            criteria.setFromDate(fromDate.atStartOfDay());
        }
        if (toDate != null) {
            criteria.setToDate(toDate.atTime(LocalTime.MAX));
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "receivedDate"));
        return feedbackService.search(criteria, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER','RECEIVER','HANDLER','VIEWER')")
    public FeedbackDetailResponse get(@PathVariable Long id) {
        return feedbackService.getDetail(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEIVER')")
    public FeedbackDetailResponse create(@Valid @RequestBody FeedbackCreateRequest request) {
        return feedbackService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public FeedbackDetailResponse update(@PathVariable Long id, @Valid @RequestBody FeedbackUpdateRequest request) {
        return feedbackService.update(id,request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        feedbackService.delete(id);
        return ApiResponse.ok("Feedback deleted");
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','RECEIVER')")
    public FeedbackDetailResponse assign(@PathVariable Long id,
                                         @Valid @RequestBody FeedbackAssignRequest request,
                                         Authentication authentication) {
        Long actorId = getCurrentUserId(authentication);
        return feedbackService.assign(id, request, actorId);
    }

    @PutMapping("/{id}/processing")
    @PreAuthorize("hasRole('HANDLER')")
    public FeedbackDetailResponse process(@PathVariable Long id,
                                          @Valid @RequestBody FeedbackProcessingRequest request,
                                          Authentication authentication) {
        Long actorId = getCurrentUserId(authentication);
        return feedbackService.process(id, request, actorId);
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER','RECEIVER','HANDLER','VIEWER')")
    public List<FeedbackHistoryItem> history(@PathVariable Long id) {
        return feedbackService.history(id);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('HANDLER','ADMIN')")
    public PageResponse<FeedbackListItemResponse> myFeedbacks(Authentication authentication,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        Long handlerId = getCurrentUserId(authentication);
        FeedbackFilterCriteria criteria = new FeedbackFilterCriteria();
        criteria.setHandlerId(handlerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "receivedDate"));
        return feedbackService.search(criteria, pageable);
    }

    private Long getCurrentUserId(Authentication authentication) {
        UserDto user = userDtoRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return (long) user.getId();
    }
}

