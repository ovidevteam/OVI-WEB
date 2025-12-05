package ovi.web.dhybe.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;
import ovi.web.dhybe.dto.auth.UserDto;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ratings")
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rat_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", nullable = false, unique = true)
    private Feedback feedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Min(1)
    @Max(5)
    @Column(columnDefinition = "SMALLINT",nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rated_by", nullable = false)
    private UserDto ratedBy;

    @Column(name = "rated_date")
    private LocalDateTime ratedDate;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column(name = "modified_by", length = 50)
    private String modifiedBy;

    @PrePersist
    public void onCreate() {
        createdDate = LocalDateTime.now();
        ratedDate = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        modifyDate = LocalDateTime.now();
    }
}

