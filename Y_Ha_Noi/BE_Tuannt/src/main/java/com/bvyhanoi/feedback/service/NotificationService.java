package com.bvyhanoi.feedback.service;

import com.bvyhanoi.feedback.dto.NotificationDTO;
import com.bvyhanoi.feedback.entity.Feedback;
import com.bvyhanoi.feedback.entity.Notification;
import com.bvyhanoi.feedback.entity.User;
import com.bvyhanoi.feedback.repository.FeedbackRepository;
import com.bvyhanoi.feedback.repository.NotificationRepository;
import com.bvyhanoi.feedback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    public List<NotificationDTO> getNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
    
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
        
        notification.setRead(true);
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
    
    @Transactional
    public void createFeedbackNotification(Long feedbackId, String message) {
        Feedback feedback = feedbackRepository.findById(feedbackId).orElse(null);
        if (feedback == null) return;
        
        // Notify ADMIN and LEADER
        List<User> adminsAndLeaders = userRepository.findAll().stream()
            .filter(u -> (u.getRole() == User.Role.ADMIN || u.getRole() == User.Role.LEADER) 
                      && u.getStatus() == User.UserStatus.ACTIVE)
            .collect(Collectors.toList());
        
        for (User user : adminsAndLeaders) {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setType(Notification.NotificationType.FEEDBACK);
            notification.setTitle("Phản ánh mới");
            notification.setMessage(message);
            notification.setRead(false);
            notification.setFeedbackId(feedbackId);
            notificationRepository.save(notification);
        }
    }
    
    @Transactional
    public void createAssignedNotification(Long handlerId, Long feedbackId, String feedbackCode) {
        Notification notification = new Notification();
        notification.setUserId(handlerId);
        notification.setType(Notification.NotificationType.ASSIGNED);
        notification.setTitle("Được phân công");
        notification.setMessage("Bạn được phân công xử lý phản ánh: " + feedbackCode);
        notification.setRead(false);
        notification.setFeedbackId(feedbackId);
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void createCompletedNotification(Long feedbackId, String feedbackCode) {
        Feedback feedback = feedbackRepository.findById(feedbackId).orElse(null);
        if (feedback == null) return;
        
        // Notify ADMIN and LEADER
        List<User> adminsAndLeaders = userRepository.findAll().stream()
            .filter(u -> (u.getRole() == User.Role.ADMIN || u.getRole() == User.Role.LEADER) 
                      && u.getStatus() == User.UserStatus.ACTIVE)
            .collect(Collectors.toList());
        
        for (User user : adminsAndLeaders) {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setType(Notification.NotificationType.COMPLETED);
            notification.setTitle("Hoàn thành");
            notification.setMessage("Phản ánh " + feedbackCode + " đã được xử lý xong");
            notification.setRead(false);
            notification.setFeedbackId(feedbackId);
            notificationRepository.save(notification);
        }
    }
    
    @Transactional
    public void createRatingNotification(Long feedbackId, Long ratingId, String feedbackCode, String userName) {
        Feedback feedback = feedbackRepository.findById(feedbackId).orElse(null);
        if (feedback == null) return;
        
        // Notify ADMIN and LEADER
        List<User> adminsAndLeaders = userRepository.findAll().stream()
            .filter(u -> (u.getRole() == User.Role.ADMIN || u.getRole() == User.Role.LEADER) 
                      && u.getStatus() == User.UserStatus.ACTIVE)
            .collect(Collectors.toList());
        
        for (User user : adminsAndLeaders) {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setType(Notification.NotificationType.RATING);
            notification.setTitle("Đánh giá mới");
            notification.setMessage(userName + " đã đánh giá phản ánh: " + feedbackCode);
            notification.setRead(false);
            notification.setFeedbackId(feedbackId);
            notification.setRatingId(ratingId);
            notificationRepository.save(notification);
        }
    }
    
    private NotificationDTO toDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUserId());
        dto.setType(notification.getType());
        dto.setTitle(notification.getTitle());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.getRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setFeedbackId(notification.getFeedbackId());
        dto.setRatingId(notification.getRatingId());
        return dto;
    }
}

