package ovi.web.dhybe.entity;

import jakarta.persistence.*;
import lombok.*;
import ovi.web.dhybe.dto.auth.UserDto;
import ovi.web.dhybe.enums.FeedbackAction;
import ovi.web.dhybe.enums.FeedbackStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "feedback_logs")
public class FeedbackLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserDto user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FeedbackAction action;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private FeedbackStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 20)
    private FeedbackStatus newStatus;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @PrePersist
    public void onCreate() {
        createdDate = LocalDateTime.now();
    }
}

