package ovi.web.dhybe.entity;

import jakarta.persistence.*;
import lombok.*;
import ovi.web.dhybe.dto.auth.UserDto;
import ovi.web.dhybe.enums.FeedbackLevel;
import ovi.web.dhybe.enums.FeedbackStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "feedbacks")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "received_date", nullable = false)
    private LocalDateTime receivedDate;

    @Column(nullable = false, length = 50)
    private String channel;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackLevel level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackStatus status = FeedbackStatus.NEW;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handler_id")
    private UserDto handler;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserDto receiver;

    @Column(name = "process_note", columnDefinition = "TEXT")
    private String processNote;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    @Column(name = "assigned_date")
    private LocalDateTime assignedDate;

    @Column(name = "last_process_date")
    private LocalDateTime lastProcessDate;

    @Column(name = "process_count")
    private Integer processCount = 0;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FeedbackImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FeedbackLog> logs = new ArrayList<>();

    @OneToOne(mappedBy = "feedback", fetch = FetchType.LAZY)
    private Rating rating;

    @PrePersist
    public void onCreate() {
        createdDate = LocalDateTime.now();
    }
}

