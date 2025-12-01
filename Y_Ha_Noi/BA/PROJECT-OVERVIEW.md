# Hệ thống Quản lý Phản ánh Bệnh viện Y Hà Nội

## 📋 Thông tin dự án

**Tên dự án:** Web Quản lý Phản ánh Bệnh viện
**Khách hàng:** Bệnh viện Y Hà Nội
**Ngân sách:** 25-30 triệu VNĐ
**Ngày tạo:** 2025-11-27
**Lead BA:** Nguyễn Thanh Tuấn
**Lead BE:** Nguyễn Thanh Tuấn
**Ngày phê duyệt:** 27/11/2025

---

## 🎯 Mục tiêu dự án

Xây dựng hệ thống web đơn giản để:
- Quản lý phản ánh từ bệnh nhân/nhân viên
- Phân công xử lý theo phòng ban/bác sĩ
- Theo dõi tiến độ xử lý
- Báo cáo thống kê cho lãnh đạo

---

## 👥 Người dùng hệ thống

| Vai trò | Quyền hạn |
|---|---|
| **Admin** | Phân quyền, quản lý danh mục (phòng ban, bác sĩ, user) |
| **Lãnh đạo** | Xem báo cáo, theo dõi tiến độ |
| **Người tiếp nhận** | Nhập phản ánh từ các kênh (điện thoại, email, trực tiếp) |
| **Người xử lý** | Nhận thông báo, xử lý, ghi nhận kết quả |
| **Người theo dõi** | Xem phản ánh của phòng mình |

---

## 📊 Các chức năng chính

### 1. Phân quyền User
- Tạo tài khoản user
- Phân vai trò: Admin, Lãnh đạo, Tiếp nhận, Xử lý, Theo dõi
- Gán user vào phòng ban

### 2. Quản lý Danh mục
- **Phòng ban**: Tên phòng, trưởng phòng, người xử lý mặc định
- **Bác sĩ**: Tên, chuyên khoa, phòng ban

### 3. Nhập Phản ánh
- Nhập từ các kênh: Hotline, Email, Trực tiếp, Zalo, Facebook
- Thông tin: Nội dung, phòng liên quan, bác sĩ (nếu có), mức độ
- Upload nhiều hình ảnh (tối đa 10 ảnh/phản ánh)
- Tự động phân công người xử lý theo phòng ban

### 4. Xử lý Phản ánh
- Người xử lý nhận thông báo qua **Email**
- Cập nhật tiến độ: Chưa xử lý → Đang xử lý → Hoàn thành
- Ghi nhận kết quả xử lý
- Upload hình ảnh minh chứng (trước/sau xử lý)

### 5. Thông báo Email
- Gửi email khi có phản ánh mới
- Gửi email nhắc nhở khi quá hạn
- Gửi email khi hoàn thành

### 6. Báo cáo Thống kê
- Báo cáo theo phòng ban
- Báo cáo theo bác sĩ
- Báo cáo theo mức độ (Khẩn cấp, Cao, Trung bình, Thấp)
- Báo cáo theo trạng thái
- Thời gian xử lý trung bình
- Top 10 phản ánh nhiều nhất
- Hiển thị hình ảnh trong báo cáo

---

## 🛠️ Công nghệ (Tech Stack)

### Frontend:
- **Vue.js 3** (Composition API)
- **Vue Router** - Routing
- **Pinia** - State management
- **Axios** - HTTP client
- **Element Plus** - UI component library (đẹp, nhiều components)
- **Chart.js** hoặc **ECharts** - Biểu đồ
- **Vue Upload Component** - Upload hình ảnh

### Backend:
- **Spring Boot 3.x** (Java 17+)
- **Spring Security** - Authentication & Authorization
- **Spring Data JPA** - ORM
- **MySQL 8** hoặc **PostgreSQL** - Database
- **Spring Mail** - Gửi email
- **Lombok** - Reduce boilerplate code
- **ModelMapper** - DTO mapping

### Email:
- **JavaMail** + **Gmail SMTP** (Free cho < 500 emails/day)
- Template engine: **Thymeleaf** (cho email HTML)

### File Upload:
- **MultipartFile** (Spring Boot)
- **ImageIO** (Java) - Resize/compress ảnh
- **Local storage** (folder `uploads/` trên server)

### Hosting:
- **VPS giá rẻ**: ~200-300k/tháng (DigitalOcean, Vultr, AWS Lightsail)
- **Database**: MySQL/PostgreSQL trên cùng VPS
- **Storage**: ~20GB đủ cho hình ảnh (resize về 800x600)

### Build & Deploy:
- **Maven** - Build tool
- **Docker** - Containerization (optional)
- **Nginx** - Reverse proxy cho production

---

## 📅 Timeline dự kiến

| Phase | Thời gian | Nội dung |
|---|---|---|
| **Week 1** | BA Analysis | Thu thập yêu cầu, viết tài liệu |
| **Week 2-3** | Design | Thiết kế database, UI/UX mockups |
| **Week 4-6** | Development | Code backend + frontend |
| **Week 7** | Testing | Test chức năng, fix bugs |
| **Week 8** | Deployment | Deploy lên server, training user |

**Tổng thời gian:** 2 tháng

---

## 💰 Phân bổ ngân sách (25-30 triệu)

| Hạng mục | Chi phí | Ghi chú |
|---|---|---|
| **Development** | 15-18 triệu | 1-2 developers x 2 tháng |
| **BA/Design** | 3-4 triệu | BA + UI/UX design |
| **Hosting (1 năm)** | 2-3 triệu | VPS hoặc Shared hosting |
| **Testing** | 2 triệu | QA testing |
| **Training** | 1 triệu | Đào tạo user |
| **Dự phòng** | 2-4 triệu | Bug fixes, adjustments |

---

## 📝 Deliverables

BA cần tạo các tài liệu sau trong folder **BA-Requirements**:

1. ✅ **Business Requirements** - Yêu cầu nghiệp vụ tổng quan
2. ✅ **Functional Specs** - Đặc tả chức năng chi tiết
3. ✅ **UI/UX Design** - Thiết kế giao diện
4. ✅ **User Stories** - User stories cho từng chức năng
5. ✅ **Database Requirements** - Yêu cầu database schema
6. ✅ **API Requirements** - Yêu cầu API (nếu có)
7. ✅ **Test Cases** - Kịch bản test
8. ✅ **Mockups/Wireframes** - Hình ảnh minh họa giao diện

---

**Next step:** BA điền chi tiết vào các folders đã tạo

