package ovi.web.dhybe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ovi.web.dhybe.dto.auth.UserDto;
import ovi.web.dhybe.entity.Department;
import ovi.web.dhybe.entity.Feedback;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationMailService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withLocale(new Locale("vi", "VN"));

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:}")
    private String fromAddress;

    @Value("${app.mail.portal-url:http://localhost:4200}")
    private String portalUrl;

    public void sendFeedbackCreated(Feedback feedback) {
        Set<String> recipients = new LinkedHashSet<>();
        addHandlerEmail(recipients, feedback.getHandler());
        addDepartmentEmail(recipients, feedback.getDepartment());
        if (recipients.isEmpty()) {
            log.debug("Skip feedback created email for feedback {} - no recipients", feedback.getCode());
            return;
        }
        String subject = "[DHY] Phản ánh mới " + feedback.getCode();
        String body = """
                Hệ thống vừa ghi nhận phản ánh %s.
                Mức độ: %s
                Phòng ban: %s
                Nội dung: %s

                Link xử lý: %s
                """.formatted(
                feedback.getCode(),
                feedback.getLevel(),
                feedback.getDepartment() != null ? feedback.getDepartment().getName() : "N/A",
                feedback.getContent(),
                buildDetailLink(feedback)
        );
        sendEmail(recipients, subject, body);
    }

    public void sendFeedbackAssigned(Feedback feedback) {
        Set<String> recipients = new LinkedHashSet<>();
        addHandlerEmail(recipients, feedback.getHandler());
        if (recipients.isEmpty()) {
            log.debug("Skip feedback assigned email for feedback {} - no handler email", feedback.getCode());
            return;
        }
        String subject = "[DHY] Phản ánh " + feedback.getCode() + " đã được giao";
        String body = """
                Xin chào %s,

                Bạn vừa được giao xử lý phản ánh %s.
                Mức độ: %s
                Phòng ban: %s
                Nội dung: %s
                Ngày giao: %s

                Link xử lý: %s
                """.formatted(
                feedback.getHandler().getFullName(),
                feedback.getCode(),
                feedback.getLevel(),
                feedback.getDepartment() != null ? feedback.getDepartment().getName() : "N/A",
                feedback.getContent(),
                feedback.getAssignedDate() != null ? DATE_TIME_FORMATTER.format(feedback.getAssignedDate()) : "Chưa cập nhật",
                buildDetailLink(feedback)
        );
        sendEmail(recipients, subject, body);
    }

    public void sendFeedbackReminder(Feedback feedback) {
        Set<String> recipients = new LinkedHashSet<>();
        addHandlerEmail(recipients, feedback.getHandler());
        if (recipients.isEmpty()) {
            return;
        }
        String subject = "[DHY] Nhắc xử lý phản ánh " + feedback.getCode();
        String body = """
                Xin chào %s,

                Phản ánh %s (mức độ %s) vẫn đang ở trạng thái %s kể từ ngày %s.
                Vui lòng đăng nhập hệ thống và cập nhật tiến độ.

                Link xử lý: %s
                """.formatted(
                feedback.getHandler().getFullName(),
                feedback.getCode(),
                feedback.getLevel(),
                feedback.getStatus(),
                feedback.getAssignedDate() != null ? DATE_TIME_FORMATTER.format(feedback.getAssignedDate()) : "Chưa xác định",
                buildDetailLink(feedback)
        );
        sendEmail(recipients, subject, body);
    }

    public void sendFeedbackCompleted(Feedback feedback) {
        Set<String> recipients = new LinkedHashSet<>();
        if (feedback.getReceiver() != null) {
            addEmail(recipients, feedback.getReceiver().getEmail());
        }
        addDepartmentEmail(recipients, feedback.getDepartment());
        if (recipients.isEmpty()) {
            log.debug("Skip feedback completed email for feedback {} - no receiver/department email", feedback.getCode());
            return;
        }
        String subject = "[DHY] Phản ánh " + feedback.getCode() + " đã hoàn thành";
        String body = """
                Phản ánh %s đã được đánh dấu hoàn thành ở trạng thái %s lúc %s.
                Người xử lý: %s
                Ghi chú xử lý: %s

                Link chi tiết: %s
                """.formatted(
                feedback.getCode(),
                feedback.getStatus(),
                feedback.getCompletedDate() != null ? DATE_TIME_FORMATTER.format(feedback.getCompletedDate()) : "Chưa cập nhật",
                feedback.getHandler() != null ? feedback.getHandler().getFullName() : "Chưa xác định",
                feedback.getProcessNote() != null ? feedback.getProcessNote() : "Không có",
                buildDetailLink(feedback)
        );
        sendEmail(recipients, subject, body);
    }

    private void addHandlerEmail(Set<String> recipients, @Nullable UserDto handler) {
        if (handler != null) {
            addEmail(recipients, handler.getEmail());
        }
    }

    private void addDepartmentEmail(Set<String> recipients, @Nullable Department department) {
        if (department != null) {
            addEmail(recipients, department.getNotificationEmail());
        }
    }

    private void addEmail(Set<String> recipients, @Nullable String email) {
        if (StringUtils.hasText(email)) {
            recipients.add(email.trim());
        }
    }

    private void sendEmail(Set<String> recipients, String subject, String body) {
        if (recipients.isEmpty()) {
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipients.toArray(String[]::new));
            message.setSubject(subject);
            message.setText(body);
            if (StringUtils.hasText(fromAddress)) {
                message.setFrom(fromAddress);
            }
            mailSender.send(message);
        } catch (MailException ex) {
            log.error("Failed to send email '{}' to {}: {}", subject, recipients, ex.getMessage(), ex);
        }
    }

    private String buildDetailLink(Feedback feedback) {
        if (feedback.getId() == null || !StringUtils.hasText(portalUrl)) {
            return "Vui lòng đăng nhập hệ thống";
        }
        String normalized = portalUrl.endsWith("/") ? portalUrl.substring(0, portalUrl.length() - 1) : portalUrl;
        return normalized + "/feedbacks/" + feedback.getId();
    }
}





