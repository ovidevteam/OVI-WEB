package com.bvyhanoi.feedback.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

	private final JavaMailSender mailSender;

	@Value("${app.mail.from:}")
	private String fromAddress;

	/**
	 * Gửi email đơn giản (text) đến một địa chỉ.
	 * Không ném exception ra ngoài để tránh làm hỏng luồng nghiệp vụ chính;
	 * lỗi sẽ được log lại.
	 */
	public void sendPlainText(String to, String subject, String body) {
		if (!StringUtils.hasText(to)) {
			log.debug("Skip sending mail, empty recipient. Subject={}", subject);
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(to.trim());
			message.setSubject(subject);
			message.setText(body);
			if (StringUtils.hasText(fromAddress)) {
				message.setFrom(fromAddress.trim());
			}
			mailSender.send(message);
		} catch (MailException ex) {
			log.error("Failed to send mail to {}: {}", to, ex.getMessage(), ex);
		}
	}

	/**
	 * Gửi email đơn giản đến nhiều địa chỉ (nếu cần).
	 */
	public void sendPlainText(String[] to, String subject, String body) {
		if (to == null || to.length == 0) {
			log.debug("Skip sending mail, empty recipients. Subject={}", subject);
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setTo(to);
			message.setSubject(subject);
			message.setText(body);
			if (StringUtils.hasText(fromAddress)) {
				message.setFrom(fromAddress.trim());
			}
			mailSender.send(message);
		} catch (MailException ex) {
			log.error("Failed to send mail to {}: {}", String.join(",", to), ex.getMessage(), ex);
		}
	}
}

