package ovi.web.dhybe.entity;

import jakarta.persistence.*;
import lombok.*;
import ovi.web.dhybe.enums.EntityStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "manager_id")
    private Long managerId;

    @Column(name = "default_handler_id")
    private Long defaultHandlerId;

    @Column(name = "notification_email", length = 100)
    private String notificationEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntityStatus status = EntityStatus.ACTIVE;

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
    }

    @PreUpdate
    public void onUpdate() {
        modifyDate = LocalDateTime.now();
    }
}





