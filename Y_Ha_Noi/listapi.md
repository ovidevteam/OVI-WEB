## Cấu hình chung

- **Base URL backend (dev)**: `http://localhost:8080/api`
- Các API có `@PreAuthorize` yêu cầu gửi header:

```bash
-H "Authorization: Bearer <JWT_TOKEN>"
```

---

## 1. Auth & User

### 1.1 Đăng nhập (FE sử dụng)

`POST /api/auth/login`

```bash
curl -X POST "http://localhost:8080/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

Trả về:

```json
{
  "token": "jwt-token",
  "user": {
    "id": 1,
    "username": "admin",
    "fullName": "Quản trị viên",
    "email": "admin@bvyhanoi.vn",
    "role": "ADMIN",
    "departmentId": null,
    "status": "ACTIVE"
  }
}
```

### 1.2 Lấy thông tin user hiện tại

`GET /api/auth/me`

```bash
curl -X GET "http://localhost:8080/api/auth/me" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 1.3 Đổi mật khẩu

`PUT /api/auth/change-password`

```bash
curl -X PUT "http://localhost:8080/api/auth/change-password" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "oldPass123",
    "newPassword": "newPass123",
    "confirmPassword": "newPass123"
  }'
```

### 1.4 Đăng xuất (no-op, FE tự xoá token)

`POST /api/auth/logout`

```bash
curl -X POST "http://localhost:8080/api/auth/logout" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 1.5 Lấy danh sách handler theo phòng ban

`GET /api/users/handlers`

```bash
curl -X GET "http://localhost:8080/api/users/handlers?departmentId=1" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 1.6 Đăng nhập public (nếu cần test riêng)

`POST /api/public/login`

```bash
curl -X POST "http://localhost:8080/api/public/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

---

## 2. Phòng ban (Departments)

### 2.1 Danh sách phòng ban

`GET /api/departments`

```bash
curl -X GET "http://localhost:8080/api/departments?status=ACTIVE" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 2.2 Lấy chi tiết phòng ban

`GET /api/departments/{id}`

```bash
curl -X GET "http://localhost:8080/api/departments/1" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 2.3 Tạo phòng ban

`POST /api/departments`

```bash
curl -X POST "http://localhost:8080/api/departments" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "NOI",
    "name": "Khoa Nội",
    "managerId": 1,
    "defaultHandlerId": 3,
    "notificationEmail": "noi@bvyhanoi.vn",
    "status": "ACTIVE"
  }'
```

### 2.4 Cập nhật phòng ban

`PUT /api/departments/{id}`

```bash
curl -X PUT "http://localhost:8080/api/departments/1" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "NOI",
    "name": "Khoa Nội tổng hợp",
    "managerId": 1,
    "defaultHandlerId": 3,
    "notificationEmail": "noi@bvyhanoi.vn",
    "status": "ACTIVE"
  }'
```

### 2.5 Xoá phòng ban

`DELETE /api/departments/{id}`

```bash
curl -X DELETE "http://localhost:8080/api/departments/1" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## 3. Bác sĩ (Doctors)

### 3.1 Danh sách bác sĩ

`GET /api/doctors`

```bash
curl -X GET "http://localhost:8080/api/doctors?departmentId=1&status=ACTIVE" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 3.2 Lấy chi tiết bác sĩ

`GET /api/doctors/{id}`

```bash
curl -X GET "http://localhost:8080/api/doctors/1" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 3.3 Tạo bác sĩ

`POST /api/doctors`

```bash
curl -X POST "http://localhost:8080/api/doctors" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "BS001",
    "fullName": "BS. Nguyễn Văn A",
    "specialty": "Nội khoa",
    "departmentId": 1,
    "email": "bsa@bvyhanoi.vn",
    "phone": "0123456789",
    "status": "ACTIVE"
  }'
```

### 3.4 Cập nhật bác sĩ

`PUT /api/doctors/{id}`

```bash
curl -X PUT "http://localhost:8080/api/doctors/1" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "BS001",
    "fullName": "BS. Nguyễn Văn A",
    "specialty": "Nội khoa",
    "departmentId": 1,
    "email": "bsa@bvyhanoi.vn",
    "phone": "0123456789",
    "status": "ACTIVE"
  }'
```

### 3.5 Xoá bác sĩ

`DELETE /api/doctors/{id}`

```bash
curl -X DELETE "http://localhost:8080/api/doctors/1" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## 4. Phản ánh (Feedbacks)

### 4.1 Tìm kiếm / danh sách

`GET /api/feedbacks`

```bash
curl -X GET "http://localhost:8080/api/feedbacks?page=0&size=20&departmentId=1&status=NEW&level=MEDIUM" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 4.2 Lấy chi tiết phản ánh

`GET /api/feedbacks/{id}`

```bash
curl -X GET "http://localhost:8080/api/feedbacks/1" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 4.3 Tạo phản ánh

`POST /api/feedbacks`

```bash
curl -X POST "http://localhost:8080/api/feedbacks" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "channel": "HOTLINE",
    "content": "Nội dung phản ánh",
    "departmentId": 1,
    "doctorId": 1,
    "level": "MEDIUM"
  }'
```

> `code`, `receivedDate`, `receiverId` có thể bỏ trống – BE sẽ tự sinh/điền.

### 4.4 Cập nhật phản ánh (ADMIN)

`PUT /api/feedbacks/{id}`

```bash
curl -X PUT "http://localhost:8080/api/feedbacks/1" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Nội dung đã chỉnh sửa",
    "departmentId": 1,
    "doctorId": 1,
    "level": "HIGH",
    "status": "IN_PROGRESS",
    "processNote": "Đã tiếp nhận xử lý"
  }'
```
sửa api cho thích hợp (done)

### 4.5 Xoá phản ánh (ADMIN)

`DELETE /api/feedbacks/{id}`

```bash
curl -X DELETE "http://localhost:8080/api/feedbacks/1" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 4.6 Phân công xử lý

`PUT /api/feedbacks/{id}/assign`

```bash
curl -X PUT "http://localhost:8080/api/feedbacks/1/assign" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "handlerId": 3,
    "note": "Phân công cho BS. A"
  }'
```

### 4.7 Cập nhật xử lý (HANDLER)

`PUT /api/feedbacks/{id}/processing`

```bash
curl -X PUT "http://localhost:8080/api/feedbacks/1/processing" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "COMPLETED",
    "note": "Đã xử lý xong",
    "imageIds": [10, 11]
  }'
```

### 4.8 Lịch sử xử lý

`GET /api/feedbacks/{id}/history`

```bash
curl -X GET "http://localhost:8080/api/feedbacks/1/history" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 4.9 Danh sách "Của tôi" (HANDLER)

`GET /api/feedbacks/my`

```bash
curl -X GET "http://localhost:8080/api/feedbacks/my?page=0&size=20" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## 5. Upload hình ảnh

### 5.1 Upload ảnh phản ánh

`POST /api/upload/feedback-images`

```bash
curl -X POST "http://localhost:8080/api/upload/feedback-images" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -F "feedbackId=1" \
  -F "uploadedBy=3" \
  -F "file=@/path/to/image.jpg"
```

### 5.2 Upload ảnh quá trình xử lý

`POST /api/upload/process-images`

```bash
curl -X POST "http://localhost:8080/api/upload/process-images" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -F "feedbackId=1" \
  -F "uploadedBy=3" \
  -F "file=@/path/to/image.jpg"
```

### 5.3 Tải ảnh theo id

`GET /api/upload/images/{id}`

```bash
curl -X GET "http://localhost:8080/api/upload/images/10" -OJ
```

### 5.4 Xoá ảnh

`DELETE /api/upload/images/{id}`

```bash
curl -X DELETE "http://localhost:8080/api/upload/images/10" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## 6. Đánh giá bác sĩ (Ratings)

### 6.1 Danh sách phản ánh đã hoàn thành (để đánh giá)

`GET /api/ratings/completed-feedbacks`

```bash
curl -X GET "http://localhost:8080/api/ratings/completed-feedbacks?rated=false" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 6.2 Gửi đánh giá (chưa hợp lý với những nhân viên) - cho nhân viên phụ trách chính dưới danh nghĩa bác sĩ

`POST /api/ratings`

```bash 
curl -X POST "http://localhost:8080/api/ratings" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "feedbackId": 1,
    "doctorId": 1,
    "rating": 5,
    "comment": "Rất hài lòng"
  }'
```

### 6.3 Cập nhật đánh giá

`PUT /api/ratings/{id}`

```bash
curl -X PUT "http://localhost:8080/api/ratings/1" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "rating": 4,
    "comment": "Cập nhật đánh giá"
  }'
```

### 6.4 Lấy đánh giá theo phản ánh

`GET /api/ratings/by-feedback/{feedbackId}`

```bash
curl -X GET "http://localhost:8080/api/ratings/by-feedback/1" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 6.5 Danh sách đánh giá của bác sĩ

`GET /api/ratings/doctor/{doctorId}`

```bash
curl -X GET "http://localhost:8080/api/ratings/doctor/1?page=0&size=20" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 6.6 Thống kê đánh giá

`GET /api/ratings/statistics`

```bash
curl -X GET "http://localhost:8080/api/ratings/statistics?limit=5" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

## 7. Báo cáo (Reports)

### 7.1 Dashboard tổng quan

`GET /api/reports/dashboard`

```bash
curl -X GET "http://localhost:8080/api/reports/dashboard" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 7.2 Báo cáo theo phòng ban

`GET /api/reports/by-department`

```bash
curl -X GET "http://localhost:8080/api/reports/by-department" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 7.3 Báo cáo theo bác sĩ

`GET /api/reports/by-doctor`

```bash
curl -X GET "http://localhost:8080/api/reports/by-doctor" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 7.4 Báo cáo phản ánh có hình ảnh

`GET /api/reports/with-images`

```bash
curl -X GET "http://localhost:8080/api/reports/with-images?departmentId=1&imageType=FEEDBACK" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 7.5 Thống kê theo tháng

`GET /api/reports/monthly-stats`

```bash
curl -X GET "http://localhost:8080/api/reports/monthly-stats?year=2025" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```


