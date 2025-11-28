# BA Requirements - Hệ thống Quản lý Phản ánh Bệnh viện Y Hà Nội

## 📋 Tổng quan Dự án

**Khách hàng:** Bệnh viện Y Hà Nội
**Ngân sách:** 25-30 triệu VNĐ
**Thời gian:** 2 tháng
**Tech Stack:** Vue.js 3 + Spring Boot 3 + MySQL

---

## 📂 Tài liệu BA

| File | Mô tả | Trạng thái |
|---|---|---|
| **PROJECT-OVERVIEW.md** | Tổng quan dự án, mục tiêu, timeline | ✅ Hoàn thành |
| **01-BUSINESS-REQUIREMENTS.md** | Yêu cầu nghiệp vụ chi tiết | ✅ Hoàn thành |
| **02-FUNCTIONAL-SPECS.md** | Đặc tả chức năng từng module | ✅ Hoàn thành |
| **03-TECHNICAL-ARCHITECTURE.md** | Kiến trúc kỹ thuật, API, Database | ✅ Hoàn thành |
| **04-USER-STORIES.md** | User stories cho DEV | ✅ Hoàn thành |
| **05-DATABASE-SCHEMA.sql** | SQL tạo database | ⏳ Cần tạo |
| **06-API-SPECS.md** | API documentation chi tiết | ⏳ Cần tạo |
| **07-UI-MOCKUPS/** | Hình ảnh mockups giao diện | ⏳ Cần tạo |

---

## 🎯 Chức năng Chính

1. ✅ **Phân quyền User** (Admin, Lãnh đạo, Tiếp nhận, Xử lý, Theo dõi)
2. ✅ **Nhập Phòng ban, Bác sĩ** (Danh mục master data)
3. ✅ **Ghi nhận Phản ánh** (Từ các kênh: Hotline, Email, Trực tiếp...)
4. ✅ **Phân công Xử lý** (Tự động theo phòng, có thể đổi thủ công)
5. ✅ **Thông báo Email** (Phản ánh mới, Nhắc nhở, Hoàn thành)
6. ✅ **Xử lý & Ghi nhận kết quả** (Cập nhật tiến độ, upload hình minh chứng)
7. ✅ **Báo cáo Thống kê** (Dashboard, theo phòng, theo bác sĩ, kèm hình ảnh)
8. ✅ **Upload & Xem nhiều hình ảnh** (Max 10 ảnh/phản ánh, lightbox gallery)

---

## 💻 Tech Stack

### Frontend:
- **Vue.js 3** (Composition API)
- **Element Plus** (UI components)
- **Chart.js** (Biểu đồ)
- **Vite** (Build tool)

### Backend:
- **Spring Boot 3**
- **Spring Security** (JWT authentication)
- **Spring Data JPA** (ORM)
- **MySQL 8** (Database)
- **JavaMail** (Email notification)

### Deployment:
- **VPS** (~$10/month)
- **Nginx** (Web server)
- **Docker** (Optional)

---

## 📊 Modules

| Module | Chức năng | Priority |
|---|---|---|
| **Auth** | Login, Logout, Session | P0 (Cao nhất) |
| **User Management** | CRUD users, phân quyền | P0 |
| **Departments** | CRUD phòng ban | P0 |
| **Doctors** | CRUD bác sĩ | P1 |
| **Feedbacks** | Nhập, xem, xử lý phản ánh | P0 |
| **Notifications** | Gửi email tự động | P1 |
| **Reports** | Dashboard, báo cáo | P1 |
| **File Upload** | Upload/view hình ảnh | P1 |

---

## 🚀 Development Plan

### Phase 1: Foundation (Week 1-2)
- Setup project (Vue + Spring Boot)
- Database schema
- Authentication (Login/Logout)
- User management (CRUD)

### Phase 2: Core Features (Week 3-4)
- Departments & Doctors management
- Feedback creation (nhập phản ánh)
- Feedback list & detail view
- File upload (hình ảnh)

### Phase 3: Processing (Week 5-6)
- "My Feedbacks" page
- Process feedback (cập nhật tiến độ)
- Email notifications
- Image gallery

### Phase 4: Reporting (Week 7)
- Dashboard
- Reports (by department, by doctor)
- Charts (Line, Bar)
- Export Excel/PDF

### Phase 5: Testing & Deployment (Week 8)
- Testing (chức năng, security, performance)
- Bug fixes
- Deploy to VPS
- User training

---

## 📞 Contacts

**Lead BA:** Nguyễn Thanh Tuấn
**Lead BE:** Nguyễn Thanh Tuấn
**Client:** Bệnh viện Y Hà Nội
**Ngày phê duyệt:** 27/11/2025

---

## 📝 Notes cho DEV

1. **Frontend sử dụng Vue.js 3 Composition API** (không dùng Options API)
2. **Backend sử dụng Spring Boot 3** (Java 17+)
3. **Database: MySQL 8** (có thể chuyển sang PostgreSQL nếu cần)
4. **Email: Gmail SMTP** (Free, cần App Password)
5. **File upload: Local storage** (không dùng S3 để tiết kiệm chi phí)
6. **Responsive: Desktop first** (vì nhập liệu nhiều)
7. **Security: JWT authentication** (token expires 24h)
8. **Image resize: Server-side** (Java ImageIO - resize về 800x600 để tiết kiệm storage)

---

**Created:** 2025-11-27
**Last Updated:** 2025-11-27
**Version:** 1.0

