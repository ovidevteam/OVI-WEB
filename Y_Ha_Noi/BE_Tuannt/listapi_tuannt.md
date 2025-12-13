## Hướng dẫn test nhanh API (BE_Tuannt)

- Đặt biến `BASE_URL=http://localhost:8080/api` (hoặc host thực tế).
- Nếu cần token: `AUTH="Authorization: Bearer <token>"`.
- Tất cả payload dùng `Content-Type: application/json` trừ upload (multipart).

### Auth (`/auth`)
- Login  
  ```bash
  curl -X POST "$BASE_URL/auth/login" -H "Content-Type: application/json" -d '{"username":"admin","password":"123456"}'
  ```
- Me (lấy thông tin)  
  ```bash
  curl "$BASE_URL/auth/me" -H "$AUTH"
  ```
- Verify token  
  ```bash
  curl "$BASE_URL/auth/verify" -H "$AUTH"
  ```
- Refresh token  
  ```bash
  curl -X POST "$BASE_URL/auth/refresh" -H "Content-Type: application/json" -d '{"refreshToken":"<refresh-token>"}'
  ```
- Đổi mật khẩu  
  ```bash
  curl -X PUT "$BASE_URL/auth/change-password" -H "Content-Type: application/json" -H "$AUTH" -d '{"oldPassword":"123","newPassword":"abc"}'
  ```
- Quên mật khẩu  
  ```bash
  curl -X POST "$BASE_URL/auth/forgot-password" -H "Content-Type: application/json" -d '{"email":"user@example.com"}'
  ```
  (đã bật gửi email qua SMTP nếu cấu hình `spring.mail.*` hợp lệ)
- Logout (chỉ client-side)  
  ```bash
  curl -X POST "$BASE_URL/auth/logout" -H "$AUTH"
  ```
- Cập nhật hồ sơ  
  ```bash
  curl -X PUT "$BASE_URL/auth/me" -H "Content-Type: application/json" -H "$AUTH" -d '{"fullName":"New Name","email":"new@example.com"}'
  ```

### Users (`/users`)
- Danh sách (có paging/filter)  
  ```bash
  curl "$BASE_URL/users?page=1&size=20&keyword=&role=&status=" -H "$AUTH"
  ```
- Chi tiết  
  ```bash
  curl "$BASE_URL/users/1" -H "$AUTH"
  ```
- Tạo mới  
  ```bash
  curl -X POST "$BASE_URL/users" -H "Content-Type: application/json" -H "$AUTH" -d '{"username":"u1","password":"123456","role":"ADMIN","email":"u1@example.com"}'
  ```
- Cập nhật  
  ```bash
  curl -X PUT "$BASE_URL/users/1" -H "Content-Type: application/json" -H "$AUTH" -d '{"email":"updated@example.com"}'
  ```
- Xóa  
  ```bash
  curl -X DELETE "$BASE_URL/users/1" -H "$AUTH"
  ```
- Reset mật khẩu  
  ```bash
  curl -X PUT "$BASE_URL/users/1/reset-password" -H "$AUTH"
  ```
- Chuyển trạng thái  
  ```bash
  curl -X PUT "$BASE_URL/users/1/toggle-status" -H "$AUTH"
  ```
- Lấy danh sách handler theo phòng ban (tùy chọn departmentId)  
  ```bash
  curl "$BASE_URL/users/handlers?departmentId=1" -H "$AUTH"
  ```

### Departments (`/departments`)
- Danh sách (status optional, ví dụ ACTIVE)  
  ```bash
  curl "$BASE_URL/departments?status=ACTIVE"
  ```
- Chi tiết  
  ```bash
  curl "$BASE_URL/departments/1" -H "$AUTH"
  ```
- Tạo / Cập nhật / Xóa  
  ```bash
  curl -X POST "$BASE_URL/departments" -H "Content-Type: application/json" -H "$AUTH" -d '{"name":"Khoa A"}'
  curl -X PUT "$BASE_URL/departments/1" -H "Content-Type: application/json" -H "$AUTH" -d '{"name":"Khoa B"}'
  curl -X DELETE "$BASE_URL/departments/1" -H "$AUTH"
  ```

### Doctors (`/doctors`)
- Danh sách (paging/filter)  
  ```bash
  curl "$BASE_URL/doctors?page=1&size=20&keyword=&departmentId="
  ```
- Danh sách rút gọn theo khoa  
  ```bash
  curl "$BASE_URL/doctors?departmentId=1&listOnly=true"
  ```
- Chi tiết / Tạo / Cập nhật / Xóa  
  ```bash
  curl "$BASE_URL/doctors/1" -H "$AUTH"
  curl -X POST "$BASE_URL/doctors" -H "Content-Type: application/json" -H "$AUTH" -d '{"name":"Bác sĩ A","departmentId":1}'
  curl -X PUT "$BASE_URL/doctors/1" -H "Content-Type: application/json" -H "$AUTH" -d '{"name":"Bác sĩ B","departmentId":1}'
  curl -X DELETE "$BASE_URL/doctors/1" -H "$AUTH"
  ```

### Feedbacks (`/feedbacks`)
- Danh sách (paging + filter)  
  ```bash
  curl "$BASE_URL/feedbacks?page=1&size=20&keyword=&status=&level=&channel=&departmentId=&doctorId=&dateFrom=&dateTo=&sort=receivedDate,desc" -H "$AUTH"
  ```
- Của tôi  
  ```bash
  curl "$BASE_URL/feedbacks/my" -H "$AUTH"
  ```
- Chi tiết / Theo code  
  ```bash
  curl "$BASE_URL/feedbacks/1" -H "$AUTH"
  curl "$BASE_URL/feedbacks/by-code/FB123" -H "$AUTH"
  ```
- Tạo / Cập nhật / Xóa  
  ```bash
  curl -X POST "$BASE_URL/feedbacks" -H "Content-Type: application/json" -H "$AUTH" -d '{"title":"Tiêu đề","content":"Nội dung","departmentId":1}'
  curl -X PUT "$BASE_URL/feedbacks/1" -H "Content-Type: application/json" -H "$AUTH" -d '{"title":"Mới","content":"Nội dung"}'
  curl -X DELETE "$BASE_URL/feedbacks/1" -H "$AUTH"
  ```
- Giao xử lý / Chốt xử lý / Cập nhật đang xử lý  
  ```bash
  curl -X PUT "$BASE_URL/feedbacks/1/assign" -H "Content-Type: application/json" -H "$AUTH" -d '{"handlerId":2}'
  curl -X PUT "$BASE_URL/feedbacks/1/process" -H "Content-Type: application/json" -H "$AUTH" -d '{"result":"Đã xử lý"}'
  curl -X PUT "$BASE_URL/feedbacks/1/processing" -H "Content-Type: application/json" -H "$AUTH" -d '{"note":"Đang làm"}'
  ```
- Lịch sử  
  ```bash
  curl "$BASE_URL/feedbacks/1/history" -H "$AUTH"
  ```

### Ratings (`/ratings`)
- Feedback đã hoàn thành (filter hasRating/department/doctor)  
  ```bash
  curl "$BASE_URL/ratings/completed-feedbacks?page=1&size=10&hasRating=&departmentId=&doctorId=" -H "$AUTH"
  ```
- Tạo / Cập nhật rating  
  ```bash
  curl -X POST "$BASE_URL/ratings" -H "Content-Type: application/json" -H "$AUTH" -d '{"feedbackId":1,"score":5,"comment":"Tốt"}'
  curl -X PUT "$BASE_URL/ratings/1" -H "Content-Type: application/json" -H "$AUTH" -d '{"score":4,"comment":"Ok"}'
  ```
- Lấy rating theo feedback  
  ```bash
  curl "$BASE_URL/ratings/by-feedback/1" -H "$AUTH"
  curl "$BASE_URL/ratings/by-feedback/1/all" -H "$AUTH"
  ```
- Thống kê bác sĩ  
  ```bash
  curl "$BASE_URL/ratings/doctor/1/average"
  curl "$BASE_URL/ratings/doctor/1?page=1&size=20"
  ```
- Thống kê tổng hợp  
  ```bash
  curl "$BASE_URL/ratings/statistics?dateFrom=&dateTo=&departmentId=" -H "$AUTH"
  ```

### Notifications (`/notifications`)
- Danh sách  
  ```bash
  curl "$BASE_URL/notifications" -H "$AUTH"
  ```
- Số chưa đọc  
  ```bash
  curl "$BASE_URL/notifications/unread-count" -H "$AUTH"
  ```
- Đánh dấu đã đọc  
  ```bash
  curl -X POST "$BASE_URL/notifications/mark-all-read" -H "$AUTH"
  curl -X PUT "$BASE_URL/notifications/1/read" -H "$AUTH"
  ```

### Upload (`/upload`)
- Upload ảnh feedback (multipart)  
  ```bash
  curl -X POST "$BASE_URL/upload/feedback-images" -H "$AUTH" -F "files=@/path/img1.jpg" -F "files=@/path/img2.jpg" -F "feedbackId=1"
  ```
- Upload ảnh quá trình  
  ```bash
  curl -X POST "$BASE_URL/upload/process-images" -H "$AUTH" -F "files=@/path/img1.jpg" -F "feedbackId=1"
  ```
- Xóa ảnh / Lấy ảnh  
  ```bash
  curl -X DELETE "$BASE_URL/upload/images/1" -H "$AUTH"
  curl "$BASE_URL/upload/images/example.jpg"
  ```

### Reports (`/reports`)
- Dashboard / Monthly / By-department / By-doctor / With-images  
  ```bash
  curl "$BASE_URL/reports/dashboard" -H "$AUTH"
  curl "$BASE_URL/reports/monthly-stats?year=2024" -H "$AUTH"
  curl "$BASE_URL/reports/by-department?dateFrom=&dateTo=&departmentId=&limit=" -H "$AUTH"
  curl "$BASE_URL/reports/by-doctor?dateFrom=&dateTo=&departmentId=" -H "$AUTH"
  curl "$BASE_URL/reports/with-images?dateFrom=&dateTo=&departmentId=" -H "$AUTH"
  ```
- Export (hiện trả file rỗng, cần backend bổ sung)  
  ```bash
  curl "$BASE_URL/reports/export-excel?dateFrom=&dateTo=&type=" -H "$AUTH" -OJ
  curl "$BASE_URL/reports/export-pdf?dateFrom=&dateTo=&type=" -H "$AUTH" -OJ
  ```

### Admin (`/admin`)
- Xóa dữ liệu demo (cảnh báo)  
  ```bash
  curl -X DELETE "$BASE_URL/admin/data/demo" -H "$AUTH"
  ```
- Thống kê số lượng bản ghi  
  ```bash
  curl "$BASE_URL/admin/data/stats" -H "$AUTH"
  ```


