package ovi.web.dhybe.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ovi.web.dhybe.dto.auth.UserDto;
import ovi.web.dhybe.dto.common.PageResponse;
import ovi.web.dhybe.dto.rating.*;
import ovi.web.dhybe.repository.UserDtoRepository;
import ovi.web.dhybe.service.RatingService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;
    private final UserDtoRepository userDtoRepository;

    @GetMapping("/completed-feedbacks")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    public List<CompletedFeedbackResponse> completedFeedbacks(@RequestParam(defaultValue = "false") boolean rated) {
        return ratingService.listCompletedFeedbacks(rated);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LEADER','HANDLER')")
    public RatingResponse create(@Valid @RequestBody RatingCreateRequest request,
                                 Authentication authentication) {
        return ratingService.create(getCurrentUserId(authentication), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    public RatingResponse update(@PathVariable Long id,
                                 @Valid @RequestBody RatingUpdateRequest request,
                                 Authentication authentication) {
        return ratingService.update(id, request, getCurrentUserId(authentication));
    }

    @GetMapping("/by-feedback/{feedbackId}")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER','RECEIVER','HANDLER','VIEWER')")
    public RatingResponse getByFeedback(@PathVariable Long feedbackId) {
        return ratingService.getByFeedback(feedbackId);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER','RECEIVER','HANDLER','VIEWER')")
    public PageResponse<RatingResponse> doctorRatings(@PathVariable Long doctorId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ratingService.doctorRatings(doctorId, pageable);
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN','LEADER')")
    public RatingStatisticsResponse statistics(@RequestParam(required = false) LocalDate fromDate,
                                               @RequestParam(required = false) LocalDate toDate,
                                               @RequestParam(defaultValue = "5") int limit) {
        return ratingService.statistics(fromDate, toDate, limit);
    }

    private Long getCurrentUserId(Authentication authentication) {
        UserDto user = userDtoRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return (long) user.getId();
    }
}

