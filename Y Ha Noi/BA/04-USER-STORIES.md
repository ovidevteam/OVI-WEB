# User Stories - Hệ thống Quản lý Phản ánh

## Epic 1: Quản lý User & Phân quyền

### US-001: Đăng nhập hệ thống
**As a** User
**I want to** Đăng nhập vào hệ thống
**So that** Tôi có thể sử dụng các chức năng theo quyền của mình

**Acceptance Criteria:**
- Màn hình login có username và password
- Hiển thị lỗi nếu sai thông tin
- Redirect đến dashboard sau khi login thành công
- Session timeout sau 30 phút không hoạt động

---

### US-002: Quản lý tài khoản User (Admin)
**As an** Admin
**I want to** Tạo và quản lý tài khoản user
**So that** Tôi có thể phân quyền cho nhân viên

**Acceptance Criteria:**
- Thêm/Sửa/Xóa user
- Phân vai trò: Admin, Lãnh đạo, Tiếp nhận, Xử lý, Theo dõi
- Gán user vào phòng ban
- Khóa/Mở khóa tài khoản

---

## Epic 2: Quản lý Danh mục

### US-003: Quản lý Phòng ban
**As an** Admin
**I want to** Quản lý danh sách phòng ban
**So that** Hệ thống có thể phân công xử lý đúng phòng

**Acceptance Criteria:**
- CRUD phòng ban
- Chỉ định người xử lý mặc định
- Chỉ định email nhận thông báo

---

### US-004: Quản lý Bác sĩ
**As an** Admin
**I want to** Quản lý danh sách bác sĩ
**So that** Có thể ghi nhận phản ánh liên quan đến bác sĩ cụ thể

**Acceptance Criteria:**
- CRUD bác sĩ
- Gán bác sĩ vào phòng ban
- Filter bác sĩ theo phòng khi nhập phản ánh

---

## Epic 3: Nhập & Quản lý Phản ánh

### US-005: Nhập Phản ánh mới
**As a** Người tiếp nhận
**I want to** Nhập phản ánh từ các kênh (hotline, email...)
**So that** Lưu lại thông tin và phân công xử lý

**Acceptance Criteria:**
- Form nhập: Kênh, Nội dung, Phòng, Bác sĩ, Mức độ
- Upload tối đa 10 hình ảnh
- Tự động sinh số PA (PA-YYYYMMDD-XXX)
- Tự động phân công người xử lý theo phòng
- Gửi email thông báo cho người xử lý

---

### US-006: Xem danh sách Phản ánh
**As a** User
**I want to** Xem danh sách phản ánh
**So that** Tôi có thể theo dõi và tìm kiếm phản ánh

**Acceptance Criteria:**
- Hiển thị table với đầy đủ thông tin
- Filter theo: Ngày, Phòng, Bác sĩ, Trạng thái, Mức độ
- Sắp xếp theo ngày (mới nhất trước)
- Pagination (20 records/page)
- Click vào row → Xem chi tiết

---

### US-007: Xem chi tiết Phản ánh
**As a** User
**I want to** Xem chi tiết một phản ánh
**So that** Tôi có thể thấy đầy đủ thông tin và hình ảnh

**Acceptance Criteria:**
- Hiển thị đầy đủ thông tin phản ánh
- Gallery hình ảnh (click để phóng to)
- Timeline trạng thái (Chưa xử lý → Đang xử lý → Hoàn thành)
- Lịch sử xử lý (log)
- Kết quả xử lý + hình ảnh minh chứng

---

## Epic 4: Xử lý Phản ánh

### US-008: Nhận thông báo Email
**As a** Người xử lý
**I want to** Nhận email khi có phản ánh mới
**So that** Tôi biết cần xử lý và không bỏ sót

**Acceptance Criteria:**
- Email gửi ngay khi có phản ánh mới
- Email chứa: Số PA, Nội dung, Mức độ, Link xử lý
- Email nhắc nhở sau 3 ngày nếu chưa xử lý

---

### US-009: Xử lý Phản ánh
**As a** Người xử lý
**I want to** Cập nhật tiến độ và kết quả xử lý
**So that** Ghi nhận quá trình xử lý và hoàn thành công việc

**Acceptance Criteria:**
- Cập nhật trạng thái: Chưa xử lý → Đang xử lý → Hoàn thành
- Nhập ghi chú xử lý
- Upload hình ảnh minh chứng (trước/sau)
- Gửi email khi hoàn thành

---

### US-010: Danh sách "Phản ánh của tôi"
**As a** Người xử lý
**I want to** Xem danh sách phản ánh được giao cho tôi
**So that** Tôi biết cần xử lý những gì

**Acceptance Criteria:**
- Chỉ hiển thị phản ánh assigned cho current user
- Highlight phản ánh quá hạn (màu đỏ)
- Sắp xếp: Khẩn cấp → Cao → Trung bình → Thấp
- Badge số lượng chưa xử lý

---

## Epic 5: Báo cáo & Thống kê

### US-011: Dashboard tổng quan (Lãnh đạo)
**As a** Lãnh đạo
**I want to** Xem dashboard tổng quan
**So that** Nắm bắt tình hình phản ánh và hiệu suất xử lý

**Acceptance Criteria:**
- 4 cards: Tổng PA tháng, Đang xử lý, Hoàn thành, Quá hạn
- Line chart: Phản ánh 12 tháng
- Bar chart: Top 5 phòng
- Table: 10 PA mới nhất

---

### US-012: Báo cáo theo Phòng ban
**As a** Lãnh đạo
**I want to** Xem báo cáo chi tiết theo phòng ban
**So that** Đánh giá hiệu suất xử lý của từng phòng

**Acceptance Criteria:**
- Table: Phòng, Tổng PA, Hoàn thành, Quá hạn, Thời gian TB, Tỷ lệ %
- Filter theo tháng/quý/năm
- Export Excel

---

### US-013: Báo cáo có Hình ảnh
**As a** Lãnh đạo
**I want to** Xem báo cáo kèm hình ảnh
**So that** Có minh chứng trực quan về vấn đề và kết quả xử lý

**Acceptance Criteria:**
- Danh sách PA kèm thumbnail hình ảnh
- Click vào PA → Xem gallery đầy đủ
- Export PDF kèm hình ảnh (thumbnail)
- Download tất cả hình ảnh (ZIP)

---

## Epic 6: Upload & Gallery

### US-014: Upload nhiều hình ảnh
**As a** User
**I want to** Upload nhiều hình ảnh cùng lúc
**So that** Minh chứng đầy đủ vấn đề hoặc kết quả xử lý

**Acceptance Criteria:**
- Drag & drop upload
- Preview trước khi upload
- Progress bar
- Validate: Max 10 ảnh, < 5MB/ảnh, format jpg/png/webp
- Xóa ảnh đã upload

---

### US-015: Xem Gallery hình ảnh
**As a** User
**I want to** Xem hình ảnh phóng to với lightbox
**So that** Quan sát chi tiết vấn đề

**Acceptance Criteria:**
- Grid gallery trong chi tiết PA
- Click ảnh → Lightbox (phóng to)
- Navigate prev/next trong lightbox
- Zoom in/out
- Download ảnh gốc

---

**Total User Stories:** 15
**Priority:** High (US-005, US-008, US-009, US-011)
**Lead BA:** Nguyễn Thanh Tuấn
**Lead BE:** Nguyễn Thanh Tuấn
**Ngày phê duyệt:** 27/11/2025
**Next step:** BA tạo Acceptance Criteria & Test Cases chi tiết

