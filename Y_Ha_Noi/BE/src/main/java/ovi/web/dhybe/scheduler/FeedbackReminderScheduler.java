package ovi.web.dhybe.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ovi.web.dhybe.entity.Feedback;
import ovi.web.dhybe.enums.FeedbackStatus;
import ovi.web.dhybe.repository.FeedbackRepository;
import ovi.web.dhybe.service.NotificationMailService;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class FeedbackReminderScheduler {

    private static final Set<FeedbackStatus> REMINDER_STATUSES =
            EnumSet.of(FeedbackStatus.ASSIGNED, FeedbackStatus.IN_PROGRESS);

    private final FeedbackRepository feedbackRepository;
    private final NotificationMailService notificationMailService;

    @Value("${app.mail.reminder-days:3}")
    private long reminderAfterDays;

    @Scheduled(cron = "${app.mail.reminder-cron:0 0 8 * * *}")
    @Transactional(readOnly = true)
    public void sendReminders() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(reminderAfterDays);
        long count = 0;
        for (FeedbackStatus status : REMINDER_STATUSES) {
            List<Feedback> feedbacks = feedbackRepository.findByStatus(status);
            for (Feedback feedback : feedbacks) {
                if (feedback.getAssignedDate() == null) {
                    continue;
                }
                if (feedback.getAssignedDate().isBefore(threshold) && feedback.getCompletedDate() == null) {
                    notificationMailService.sendFeedbackReminder(feedback);
                    count++;
                }
            }
        }
        if (count > 0) {
            log.info("Sent {} reminder emails for feedback older than {} days", count, reminderAfterDays);
        }
    }
}





