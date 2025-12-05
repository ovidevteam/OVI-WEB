package ovi.web.dhybe.entity;

import jakarta.persistence.*;
import lombok.*;
import ovi.web.dhybe.dto.auth.UserDto;
import ovi.web.dhybe.enums.FeedbackImageType;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "feedback_images")
public class FeedbackImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @Column(name = "image_path", nullable = false, length = 255)
    private String imagePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 20)
    private FeedbackImageType imageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private UserDto uploadedBy;

    @Column(name = "uploaded_date")
    private LocalDateTime uploadedDate;

    @PrePersist
    public void onCreate() {
        uploadedDate = LocalDateTime.now();
    }
}

