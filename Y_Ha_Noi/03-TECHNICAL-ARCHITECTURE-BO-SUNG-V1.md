# Kiến trúc Kỹ thuật - Bổ sung V1
# Hệ thống Quản lý Phản ánh - Bệnh viện Y Hà Nội

> **Tài liệu bổ sung:** Các API và Database mới phát sinh từ FE
> **Người thực hiện:** tuannt
> **Ngày tạo:** 30/11/2025
> **Version:** 1.0

---

## 📋 Tổng quan Bổ sung

Tài liệu này mô tả các API endpoints và database tables mới được bổ sung so với tài liệu `03-TECHNICAL-ARCHITECTURE.md` gốc, dựa trên phân tích từ Frontend services.

### Danh sách Module Bổ sung:
1. **Rating Module** - Module hoàn toàn mới (đánh giá bác sĩ)
2. **Auth Module** - Bổ sung API đổi mật khẩu
3. **User Module** - Bổ sung 4 APIs
4. **Feedback Module** - Bổ sung 3 APIs
5. **Report Module** - Bổ sung 2 APIs

---

## 🗃️ Database Schema Bổ sung

### 1. Table: `ratings` (MỚI)

```sql
-- =====================================================
-- Table: ratings
-- Purpose: Lưu đánh giá bác sĩ sau khi xử lý phản ánh hoàn thành
-- Created: 30/11/2025
-- Author: tuannt
-- =====================================================

CREATE TABLE ratings (
    rat_id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    feedback_id     BIGINT NOT NULL,
    doctor_id       BIGINT NOT NULL,
    rating          TINYINT NOT NULL,
    comment         TEXT,
    rated_by        BIGINT NOT NULL,
    rated_date      DATETIME DEFAULT CURRENT_TIMESTAMP,
    -- Audit columns
    created_date    DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(50),
    modify_date     DATETIME,
    modified_by     VARCHAR(50),
    -- Foreign keys
    CONSTRAINT fk_ratings_feedback FOREIGN KEY (feedback_id) REFERENCES feedbacks(id) ON DELETE CASCADE,
    CONSTRAINT fk_ratings_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT fk_ratings_user FOREIGN KEY (rated_by) REFERENCES users(id),
    -- Constraints
    CONSTRAINT chk_rating_value CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uk_feedback_rating UNIQUE (feedback_id)
);

-- Indexes
CREATE INDEX idx_ratings_doctor ON ratings(doctor_id);
CREATE INDEX idx_ratings_rated_date ON ratings(rated_date);
CREATE INDEX idx_ratings_rating ON ratings(rating);

-- Comments
COMMENT ON TABLE ratings IS 'Bảng lưu đánh giá bác sĩ sau khi xử lý phản ánh';
COMMENT ON COLUMN ratings.rat_id IS 'Primary key';
COMMENT ON COLUMN ratings.feedback_id IS 'FK đến phản ánh được đánh giá';
COMMENT ON COLUMN ratings.doctor_id IS 'FK đến bác sĩ được đánh giá';
COMMENT ON COLUMN ratings.rating IS 'Điểm đánh giá từ 1-5 sao';
COMMENT ON COLUMN ratings.comment IS 'Nhận xét chi tiết';
COMMENT ON COLUMN ratings.rated_by IS 'Người thực hiện đánh giá';
COMMENT ON COLUMN ratings.rated_date IS 'Ngày đánh giá';
```

### 2. Bổ sung Table: `feedbacks`

```sql
-- =====================================================
-- ALTER: feedbacks
-- Purpose: Thêm các cột tracking xử lý
-- Created: 30/11/2025
-- Author: tuannt
-- =====================================================

ALTER TABLE feedbacks ADD COLUMN assigned_date DATETIME COMMENT 'Ngày assign cho người xử lý';
ALTER TABLE feedbacks ADD COLUMN last_process_date DATETIME COMMENT 'Ngày cập nhật xử lý gần nhất';
ALTER TABLE feedbacks ADD COLUMN process_count INT DEFAULT 0 COMMENT 'Số lần cập nhật xử lý';
```

### 3. Xác nhận Table: `users`

```sql
-- Đảm bảo cột status có constraint phù hợp
-- Values: ACTIVE, INACTIVE, LOCKED

ALTER TABLE users MODIFY COLUMN status VARCHAR(20) DEFAULT 'ACTIVE';
```

---

## 📡 REST API Endpoints Bổ sung

### 1. Rating APIs (Module Mới)

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/api/ratings/completed-feedbacks` | Lấy DS phản ánh hoàn thành để đánh giá | ADMIN, LEADER |
| POST | `/api/ratings` | Tạo đánh giá mới | ADMIN, LEADER, HANDLER |
| PUT | `/api/ratings/{id}` | Cập nhật đánh giá | ADMIN, LEADER |
| GET | `/api/ratings/by-feedback/{feedbackId}` | Lấy đánh giá theo phản ánh | ALL |
| GET | `/api/ratings/doctor/{doctorId}/average` | Lấy điểm TB của bác sĩ | ALL |
| GET | `/api/ratings/doctor/{doctorId}` | Lấy tất cả đánh giá của bác sĩ | ALL |
| GET | `/api/ratings/statistics` | Thống kê đánh giá | ADMIN, LEADER |

#### Chi tiết API:

```
================================================================
GET /api/ratings/completed-feedbacks
================================================================
Purpose: Lấy danh sách phản ánh đã hoàn thành để đánh giá
Auth: ADMIN, LEADER

Request Params:
- page: int (default: 0)
- size: int (default: 10)
- departmentId: Long (optional)
- doctorId: Long (optional)
- rated: Boolean (optional) - true=đã đánh giá, false=chưa đánh giá

Response:
{
    "data": [
        {
            "id": 1,
            "code": "PA-2024-001",
            "content": "Nội dung phản ánh...",
            "doctorId": 101,
            "doctorName": "BS. Nguyễn Văn A",
            "departmentId": 1,
            "departmentName": "Nội khoa",
            "completedDate": "20/11/2024",
            "rating": 4,
            "comment": "Bác sĩ xử lý tốt"
        }
    ],
    "total": 100,
    "stats": {
        "total": 100,
        "pending": 40,
        "rated": 60,
        "avgRating": 4.2
    }
}
```

```
================================================================
POST /api/ratings
================================================================
Purpose: Tạo đánh giá mới cho phản ánh đã hoàn thành
Auth: ADMIN, LEADER, HANDLER

Request Body:
{
    "feedbackId": 1,
    "doctorId": 101,
    "rating": 4,
    "comment": "Bác sĩ xử lý tốt, giải quyết nhanh chóng"
}

Response:
{
    "success": true,
    "data": {
        "id": 1,
        "feedbackId": 1,
        "doctorId": 101,
        "rating": 4,
        "comment": "Bác sĩ xử lý tốt, giải quyết nhanh chóng",
        "ratedBy": "admin",
        "ratedDate": "2024-11-30"
    }
}

Validation:
- feedbackId: required, must exist, status = COMPLETED
- doctorId: required, must exist
- rating: required, 1-5
- comment: optional, max 1000 chars
- Mỗi feedback chỉ được đánh giá 1 lần
```

```
================================================================
PUT /api/ratings/{id}
================================================================
Purpose: Cập nhật đánh giá đã có
Auth: ADMIN, LEADER

Request Body:
{
    "rating": 5,
    "comment": "Cập nhật: Xuất sắc!"
}

Response:
{
    "success": true,
    "data": { ... }
}
```

```
================================================================
GET /api/ratings/by-feedback/{feedbackId}
================================================================
Purpose: Lấy đánh giá theo phản ánh
Auth: ALL

Response (nếu có):
{
    "id": 1,
    "feedbackId": 1,
    "doctorId": 101,
    "doctorName": "BS. Nguyễn Văn A",
    "rating": 4,
    "comment": "...",
    "ratedBy": "admin",
    "ratedByName": "Quản trị viên",
    "ratedDate": "2024-11-30"
}

Response (nếu chưa có):
null
```

```
================================================================
GET /api/ratings/doctor/{doctorId}/average
================================================================
Purpose: Lấy điểm trung bình của bác sĩ
Auth: ALL

Response:
{
    "avgRating": 4.5,
    "totalRatings": 25
}
```

```
================================================================
GET /api/ratings/doctor/{doctorId}
================================================================
Purpose: Lấy tất cả đánh giá của bác sĩ
Auth: ALL

Request Params:
- page: int (default: 0)
- size: int (default: 10)

Response:
{
    "data": [
        {
            "id": 1,
            "feedbackId": 1,
            "feedbackCode": "PA-2024-001",
            "rating": 4,
            "comment": "...",
            "ratedBy": "admin",
            "ratedDate": "2024-11-20"
        }
    ],
    "total": 25
}
```

```
================================================================
GET /api/ratings/statistics
================================================================
Purpose: Thống kê đánh giá tổng hợp
Auth: ADMIN, LEADER

Request Params:
- dateFrom: String (yyyy-MM-dd)
- dateTo: String (yyyy-MM-dd)
- departmentId: Long (optional)

Response:
{
    "totalRatings": 100,
    "avgRating": 4.2,
    "ratingDistribution": {
        "1": 5,
        "2": 10,
        "3": 20,
        "4": 35,
        "5": 30
    },
    "topDoctors": [
        {
            "doctorId": 103,
            "doctorName": "BS. Lê Văn C",
            "departmentName": "Da liễu",
            "avgRating": 5.0,
            "totalRatings": 15
        },
        {
            "doctorId": 101,
            "doctorName": "BS. Nguyễn Văn A",
            "departmentName": "Nội khoa",
            "avgRating": 4.8,
            "totalRatings": 20
        }
    ],
    "byDepartment": [
        {
            "departmentId": 1,
            "departmentName": "Nội khoa",
            "avgRating": 4.5,
            "totalRatings": 30
        }
    ]
}
```

---

### 2. Auth APIs (Bổ sung)

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| PUT | `/api/auth/change-password` | Đổi mật khẩu user | ALL (authenticated) |

```
================================================================
PUT /api/auth/change-password
================================================================
Purpose: Đổi mật khẩu của user đang đăng nhập
Auth: ALL (authenticated)

Request Body:
{
    "currentPassword": "oldpass123",
    "newPassword": "newpass456",
    "confirmPassword": "newpass456"
}

Response:
{
    "success": true,
    "message": "Đổi mật khẩu thành công"
}

Validation:
- currentPassword: required, phải đúng với mật khẩu hiện tại
- newPassword: required, min 6 chars, khác currentPassword
- confirmPassword: required, phải khớp newPassword
```

---

### 3. User APIs (Bổ sung)

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/api/users/{id}` | Lấy chi tiết user | ADMIN |
| PUT | `/api/users/{id}/reset-password` | Reset mật khẩu về mặc định | ADMIN |
| PUT | `/api/users/{id}/toggle-status` | Đổi trạng thái ACTIVE ↔ INACTIVE | ADMIN |
| GET | `/api/users/handlers` | Lấy danh sách người xử lý | ALL |

```
================================================================
GET /api/users/{id}
================================================================
Purpose: Lấy chi tiết thông tin user
Auth: ADMIN

Response:
{
    "id": 1,
    "username": "admin",
    "fullName": "Quản trị viên",
    "email": "admin@bvyhanoi.vn",
    "role": "ADMIN",
    "departmentId": null,
    "departmentName": "",
    "status": "ACTIVE",
    "createdDate": "2024-01-01",
    "lastLogin": "2024-11-30"
}
```

```
================================================================
PUT /api/users/{id}/reset-password
================================================================
Purpose: Reset mật khẩu user về mặc định (hoặc random)
Auth: ADMIN

Response:
{
    "success": true,
    "message": "Reset mật khẩu thành công",
    "newPassword": "abc123xyz"
}

Note: newPassword chỉ hiển thị 1 lần, user phải đổi ngay khi đăng nhập
```

```
================================================================
PUT /api/users/{id}/toggle-status
================================================================
Purpose: Đổi trạng thái user ACTIVE ↔ INACTIVE
Auth: ADMIN

Response:
{
    "success": true,
    "newStatus": "INACTIVE",
    "message": "Đã vô hiệu hóa tài khoản"
}

Business Rules:
- Không thể toggle chính mình
- User INACTIVE không thể đăng nhập
```

```
================================================================
GET /api/users/handlers
================================================================
Purpose: Lấy danh sách người có quyền xử lý phản ánh
Auth: ALL

Request Params:
- departmentId: Long (optional) - Lọc theo phòng ban

Response:
[
    {
        "id": 4,
        "fullName": "BS. Nguyễn Văn A",
        "email": "handler@bvyhanoi.vn",
        "departmentId": 2,
        "departmentName": "Nội khoa",
        "role": "HANDLER"
    },
    {
        "id": 5,
        "fullName": "BS. Trần Thị B",
        "email": "handler2@bvyhanoi.vn",
        "departmentId": 3,
        "departmentName": "Ngoại khoa",
        "role": "HANDLER"
    }
]

Note: Chỉ trả về users có role = HANDLER và status = ACTIVE
```

---

### 4. Feedback APIs (Bổ sung)

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| PUT | `/api/feedbacks/{id}/assign` | Assign handler cho phản ánh | ADMIN, RECEIVER |
| PUT | `/api/feedbacks/{id}/processing` | Cập nhật trạng thái xử lý | HANDLER |
| GET | `/api/feedbacks/{id}/history` | Lấy lịch sử xử lý | ALL |

```
================================================================
PUT /api/feedbacks/{id}/assign
================================================================
Purpose: Assign người xử lý cho phản ánh
Auth: ADMIN, RECEIVER

Request Body:
{
    "handlerId": 4
}

Response:
{
    "success": true,
    "message": "Đã assign phản ánh cho BS. Nguyễn Văn A"
}

Business Rules:
- handlerId phải là user có role HANDLER
- Gửi email thông báo cho handler
- Ghi log vào feedback_logs
- Cập nhật assigned_date = NOW()
```

```
================================================================
PUT /api/feedbacks/{id}/processing
================================================================
Purpose: Cập nhật trạng thái xử lý phản ánh
Auth: HANDLER (assigned)

Request Body:
{
    "status": "IN_PROGRESS",
    "note": "Đang liên hệ với bệnh nhân để xác minh",
    "images": ["image1.jpg", "image2.jpg"]
}

Response:
{
    "success": true,
    "message": "Cập nhật xử lý thành công"
}

Status Values:
- NEW: Mới tạo
- ASSIGNED: Đã assign
- IN_PROGRESS: Đang xử lý
- PENDING_APPROVAL: Chờ duyệt
- COMPLETED: Hoàn thành
- REJECTED: Từ chối

Business Rules:
- Chỉ handler được assign mới được cập nhật
- Ghi log vào feedback_logs
- Cập nhật last_process_date = NOW()
- Tăng process_count += 1
- Nếu status = COMPLETED, gửi email thông báo
```

```
================================================================
GET /api/feedbacks/{id}/history
================================================================
Purpose: Lấy lịch sử xử lý của phản ánh
Auth: ALL

Response:
[
    {
        "id": 1,
        "feedbackId": 1,
        "action": "CREATED",
        "oldStatus": null,
        "newStatus": "NEW",
        "note": "Tạo phản ánh mới",
        "userId": 3,
        "userName": "Trần Thị Tiếp nhận",
        "createdDate": "2024-11-25 10:30:00"
    },
    {
        "id": 2,
        "feedbackId": 1,
        "action": "ASSIGNED",
        "oldStatus": "NEW",
        "newStatus": "ASSIGNED",
        "note": "Assign cho BS. Nguyễn Văn A",
        "userId": 1,
        "userName": "Quản trị viên",
        "createdDate": "2024-11-25 11:00:00"
    },
    {
        "id": 3,
        "feedbackId": 1,
        "action": "PROCESS_UPDATE",
        "oldStatus": "ASSIGNED",
        "newStatus": "IN_PROGRESS",
        "note": "Đang liên hệ với bệnh nhân",
        "userId": 4,
        "userName": "BS. Nguyễn Văn A",
        "createdDate": "2024-11-26 09:00:00"
    }
]

Action Values:
- CREATED: Tạo mới
- ASSIGNED: Assign handler
- PROCESS_UPDATE: Cập nhật xử lý
- STATUS_CHANGE: Thay đổi trạng thái
- COMPLETED: Hoàn thành
- IMAGE_ADDED: Thêm hình ảnh
```

---

### 5. Report APIs (Bổ sung)

| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| GET | `/api/reports/with-images` | Báo cáo phản ánh có hình ảnh | ADMIN, LEADER |
| GET | `/api/reports/monthly-stats` | Thống kê theo tháng | ADMIN, LEADER |

```
================================================================
GET /api/reports/with-images
================================================================
Purpose: Báo cáo các phản ánh có đính kèm hình ảnh
Auth: ADMIN, LEADER

Request Params:
- dateFrom: String (yyyy-MM-dd)
- dateTo: String (yyyy-MM-dd)
- departmentId: Long (optional)
- imageType: String (optional) - FEEDBACK | PROCESS | ALL

Response:
{
    "data": [
        {
            "id": 1,
            "code": "PA-2024-001",
            "content": "...",
            "departmentName": "Nội khoa",
            "status": "COMPLETED",
            "feedbackImages": [
                {
                    "id": 1,
                    "path": "/uploads/feedbacks/img1.jpg",
                    "uploadedDate": "2024-11-25"
                }
            ],
            "processImages": [
                {
                    "id": 2,
                    "path": "/uploads/process/img2.jpg",
                    "uploadedDate": "2024-11-26"
                }
            ]
        }
    ],
    "total": 50,
    "stats": {
        "totalWithImages": 50,
        "totalFeedbackImages": 120,
        "totalProcessImages": 80
    }
}
```

```
================================================================
GET /api/reports/monthly-stats
================================================================
Purpose: Thống kê phản ánh theo tháng trong năm
Auth: ADMIN, LEADER

Request Params:
- year: int (required) - Năm cần thống kê

Response:
[
    {
        "month": 1,
        "monthName": "Tháng 1",
        "total": 50,
        "completed": 45,
        "inProgress": 3,
        "overdue": 2,
        "avgProcessDays": 3.5,
        "byLevel": {
            "HIGH": 10,
            "MEDIUM": 25,
            "LOW": 15
        }
    },
    {
        "month": 2,
        "monthName": "Tháng 2",
        "total": 60,
        "completed": 55,
        "inProgress": 4,
        "overdue": 1,
        "avgProcessDays": 2.8,
        "byLevel": {
            "HIGH": 15,
            "MEDIUM": 30,
            "LOW": 15
        }
    }
    // ... tháng 3-12
]
```

---

## 🔧 Backend Implementation Guide

### 1. Cấu trúc Package Bổ sung

```
src/main/java/com/ovi/hospitalfeedback/
├── controller/
│   └── RatingController.java          # MỚI
├── service/
│   └── RatingService.java             # MỚI
├── repository/
│   └── RatingRepository.java          # MỚI
├── entity/
│   └── Rating.java                    # MỚI
├── dto/
│   ├── RatingDTO.java                 # MỚI
│   ├── RatingCreateDTO.java           # MỚI
│   ├── RatingUpdateDTO.java           # MỚI
│   └── RatingStatisticsDTO.java       # MỚI
```

### 2. Entity: Rating.java

```java
@Entity
@Table(name = "ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rat_id")
    private Long id;

    @Column(name = "feedback_id", nullable = false)
    private Long feedbackId;

    @Column(name = "doctor_id", nullable = false)
    private Long doctorId;

    @Column(name = "rating", nullable = false)
    @Min(1) @Max(5)
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "rated_by", nullable = false)
    private Long ratedBy;

    @Column(name = "rated_date")
    private LocalDateTime ratedDate;

    // Audit fields
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modify_date")
    private LocalDateTime modifyDate;

    @Column(name = "modified_by")
    private String modifiedBy;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "feedback_id", insertable = false, updatable = false)
    private Feedback feedback;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", insertable = false, updatable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rated_by", insertable = false, updatable = false)
    private User rater;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        ratedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifyDate = LocalDateTime.now();
    }
}
```

### 3. Repository: RatingRepository.java

```java
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    Optional<Rating> findByFeedbackId(Long feedbackId);

    List<Rating> findByDoctorId(Long doctorId);

    Page<Rating> findByDoctorId(Long doctorId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.doctorId = :doctorId")
    Double getAverageRatingByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.doctorId = :doctorId")
    Long countByDoctorId(@Param("doctorId") Long doctorId);

    @Query("SELECT r.rating, COUNT(r) FROM Rating r GROUP BY r.rating")
    List<Object[]> getRatingDistribution();

    @Query("SELECT r.doctorId, AVG(r.rating) as avgRating, COUNT(r) as totalRatings " +
           "FROM Rating r GROUP BY r.doctorId ORDER BY avgRating DESC")
    List<Object[]> getTopDoctorsByRating(Pageable pageable);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.ratedDate BETWEEN :dateFrom AND :dateTo")
    Long countByDateRange(@Param("dateFrom") LocalDateTime dateFrom,
                          @Param("dateTo") LocalDateTime dateTo);
}
```

---

## 📊 Tổng hợp Thay đổi

### APIs Mới: 17 endpoints

| Module | Số API Mới |
|--------|------------|
| Rating | 7 |
| Auth | 1 |
| User | 4 |
| Feedback | 3 |
| Report | 2 |
| **Tổng** | **17** |

### Database: 1 table mới + 3 cột bổ sung

| Thay đổi | Chi tiết |
|----------|----------|
| Table mới | `ratings` |
| Cột bổ sung | `feedbacks.assigned_date`, `feedbacks.last_process_date`, `feedbacks.process_count` |

---

## ✅ Checklist Implementation

### Backend Tasks:
- [ ] Tạo entity `Rating.java`
- [ ] Tạo repository `RatingRepository.java`
- [ ] Tạo service `RatingService.java`
- [ ] Tạo controller `RatingController.java`
- [ ] Tạo DTOs cho Rating module
- [ ] Bổ sung endpoints vào `AuthController.java`
- [ ] Bổ sung endpoints vào `UserController.java`
- [ ] Bổ sung endpoints vào `FeedbackController.java`
- [ ] Bổ sung endpoints vào `ReportController.java`
- [ ] Tạo migration script cho database
- [ ] Unit tests cho Rating module
- [ ] Integration tests

### Database Tasks:
- [ ] Tạo table `ratings`
- [ ] Alter table `feedbacks` - thêm 3 cột
- [ ] Tạo indexes
- [ ] Insert sample data

---

**Người thực hiện:** tuannt
**Ngày tạo:** 30/11/2025
**Phê duyệt:** (Chờ phê duyệt)
**Next step:** BE implement theo spec này

