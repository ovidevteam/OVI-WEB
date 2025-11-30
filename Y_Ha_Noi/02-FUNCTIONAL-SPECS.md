# Đặc tả Chức năng - Hệ thống Quản lý Phản ánh

## 1. Module Phân quyền User

### 1.1. Màn hình Quản lý User (Admin only)

**URL:** `/admin/users`

**Chức năng:**
- Danh sách users (table: Tên, Username, Email, Vai trò, Phòng, Trạng thái)
- Thêm user mới (form: Tên, Username, Email, Password, Vai trò, Phòng)
- Sửa user (form tương tự)
- Khóa/Mở khóa user
- Reset password

**Validation:**
- Username: Unique, 6-20 ký tự, không dấu
- Email: Valid email format, unique
- Password: Min 8 ký tự, có chữ + số

---

## 2. Module Danh mục

### 2.1. Quản lý Phòng ban

**URL:** `/admin/departments`

**Fields:**
- Mã phòng (auto: PB-001)
- Tên phòng
- Trưởng phòng (dropdown: từ danh sách user)
- Người xử lý mặc định (dropdown)
- Email nhận thông báo
- Trạng thái

### 2.2. Quản lý Bác sĩ

**URL:** `/admin/doctors`

**Fields:**
- Mã BS (auto: BS-001)
- Họ tên
- Chuyên khoa (dropdown: Nội, Ngoại, Sản, Nhi, Tim mạch...)
- Phòng ban (dropdown)
- Email
- Số điện thoại
- Trạng thái

---

## 3. Module Phản ánh

### 3.1. Nhập Phản ánh mới

**URL:** `/feedback/create`

**Form fields:**
- **Kênh tiếp nhận** (dropdown): Hotline, Email, Trực tiếp, Zalo, Facebook, Khác
- **Nội dung phản ánh** (textarea, required, max 1000 ký tự)
- **Phòng liên quan** (dropdown, required)
- **Bác sĩ liên quan** (dropdown, optional - filter theo phòng)
- **Mức độ** (radio): Khẩn cấp, Cao, Trung bình, Thấp
- **Upload hình ảnh** (file upload, max 10 ảnh, < 5MB/ảnh)
- **Ghi chú** (textarea, optional)

**Buttons:**
- [Lưu] → Lưu và gửi email cho người xử lý
- [Hủy] → Quay lại danh sách

**Auto fields:**
- Số phản ánh: PA-YYYYMMDD-XXX (auto increment theo ngày)
- Ngày tiếp nhận: Current date
- Người tiếp nhận: Current user
- Người xử lý: Lấy từ "Người xử lý mặc định" của phòng
- Trạng thái: "Chưa xử lý"

### 3.2. Danh sách Phản ánh

**URL:** `/feedback/list`

**Table columns:**
- Số PA
- Ngày
- Nội dung (50 ký tự đầu...)
- Phòng
- Bác sĩ
- Mức độ (badge màu: Đỏ, Cam, Vàng, Xanh)
- Trạng thái (badge: Chưa xử lý, Đang xử lý, Hoàn thành)
- Người xử lý
- Hành động (Xem, Sửa, Xóa)

**Filters:**
- Ngày từ - đến
- Phòng
- Bác sĩ
- Trạng thái
- Mức độ

**Sort:** Theo ngày (mới nhất trước)

### 3.3. Chi tiết Phản ánh

**URL:** `/feedback/view/{id}`

**Hiển thị:**
- **Thông tin phản ánh**: Số PA, Ngày, Kênh, Nội dung, Phòng, Bác sĩ, Mức độ
- **Hình ảnh phản ánh**: Gallery 10 ảnh (nếu có)
- **Người xử lý**: Tên, Email, SĐT
- **Trạng thái**: Timeline (Chưa xử lý → Đang xử lý → Hoàn thành)
- **Lịch sử xử lý**: Log các lần cập nhật
- **Kết quả xử lý**: Nội dung, Hình ảnh minh chứng

---

## 4. Module Xử lý

### 4.1. Phản ánh của tôi

**URL:** `/my-feedbacks`

**Hiển thị:** Danh sách phản ánh được assign cho current user

**Filters:**
- Chưa xử lý (màu đỏ)
- Đang xử lý (màu vàng)
- Hoàn thành (màu xanh)
- Quá hạn (badge đỏ, nhấp nháy)

### 4.2. Xử lý Phản ánh

**URL:** `/feedback/process/{id}`

**Form fields:**
- **Trạng thái** (dropdown): Chưa xử lý, Đang xử lý, Hoàn thành
- **Ghi nhận xử lý** (textarea, required khi chọn "Hoàn thành")
- **Upload hình ảnh** (file upload, max 10 ảnh - minh chứng xử lý)
- **Ngày hoàn thành** (date picker - chỉ khi chọn "Hoàn thành")

**Buttons:**
- [Lưu] → Lưu và gửi email (nếu hoàn thành)
- [Hủy]

---

## 5. Module Thông báo Email

### 5.1. Email template "Phản ánh mới"

**To:** Người xử lý
**CC:** Trưởng phòng
**Subject:** `[Phản ánh mới] {Số PA} - {Phòng}`

**Body:**
```
Kính gửi BS {Tên},

Có phản ánh mới cần xử lý:

📋 Thông tin:
- Số PA: {Số PA}
- Ngày nhận: {Ngày}
- Kênh: {Kênh}
- Phòng: {Tên phòng}
- Mức độ: {Mức độ}

📝 Nội dung:
{Nội dung phản ánh}

🔗 Link xử lý:
http://phananhbenhnien.vn/feedback/process/{id}

Hạn xử lý: {Ngày + 3 ngày}

Trân trọng,
Hệ thống Quản lý Phản ánh
```

### 5.2. Email template "Nhắc nhở"

**Gửi khi:** Quá 3 ngày chưa xử lý

**To:** Người xử lý
**CC:** Trưởng phòng + Lãnh đạo
**Subject:** `[Nhắc nhở] {Số PA} - Quá hạn xử lý`

### 5.3. Email template "Hoàn thành"

**To:** Người tiếp nhận
**CC:** Lãnh đạo
**Subject:** `[Hoàn thành] {Số PA} - Đã xử lý xong`

**Body:** Kèm kết quả xử lý + link xem hình ảnh

---

## 6. Module Báo cáo

### 6.1. Dashboard (Trang chủ)

**URL:** `/dashboard`

**Widgets:**
1. **Thẻ thống kê** (4 cards):
   - Tổng phản ánh tháng này
   - Đang xử lý
   - Hoàn thành tháng này
   - Quá hạn

2. **Biểu đồ Line**: Phản ánh 12 tháng gần nhất

3. **Biểu đồ Bar**: Top 5 phòng có nhiều phản ánh nhất

4. **Bảng**: 10 phản ánh mới nhất (link đến chi tiết)

### 6.2. Báo cáo theo Phòng ban

**URL:** `/reports/by-department`

**Table:**
- Tên phòng
- Tổng phản ánh
- Chưa xử lý
- Đang xử lý
- Hoàn thành
- Quá hạn
- Thời gian TB (ngày)
- Tỷ lệ hoàn thành (%)

**Export Excel:** Button [Xuất Excel]

### 6.3. Báo cáo theo Bác sĩ

**URL:** `/reports/by-doctor`

**Table:**
- Tên bác sĩ
- Phòng
- Tổng phản ánh
- Hoàn thành
- Thời gian TB (ngày)

### 6.4. Báo cáo có Hình ảnh

**URL:** `/reports/with-images`

**Hiển thị:**
- Danh sách phản ánh có hình ảnh
- Thumbnail hình ảnh (click để xem full)
- Gallery view cho từng phản ánh
- Có thể download tất cả hình ảnh (ZIP)

**Export PDF:** Báo cáo kèm hình ảnh (thumbnail)

---

## 7. Module Upload & Gallery

### 7.1. Upload Component

**Features:**
- Drag & drop upload
- Preview trước khi upload
- Progress bar khi upload
- Validate: File size, File type
- Cho phép xóa ảnh đã upload

### 7.2. Image Gallery

**Features:**
- Grid layout (3-4 ảnh/dòng)
- Lightbox khi click (xem phóng to)
- Navigate giữa các ảnh (prev/next)
- Zoom in/out
- Download ảnh gốc

---

## 📊 Database Schema (Tóm tắt)

### Tables:
1. **Users**: id, username, password, email, role, department_id, status
2. **Departments**: id, code, name, manager_id, handler_id, email, status
3. **Doctors**: id, code, name, specialty, department_id, email, phone, status
4. **Feedbacks**: id, code, date, channel, content, department_id, doctor_id, level, status, handler_id, receiver_id
5. **Feedback_Images**: id, feedback_id, image_path, type (feedback/process), uploaded_by, uploaded_date
6. **Feedback_Logs**: id, feedback_id, user_id, action, old_status, new_status, note, created_date

---

**Next step:** BA tạo UI/UX Design & User Stories

