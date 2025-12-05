# Hệ thống Quản lý Phản ánh - Bệnh viện Y Hà Nội

## Tổng quan

Hệ thống web quản lý phản ánh từ bệnh nhân/nhân viên cho Bệnh viện Y Hà Nội.

## Tech Stack

- **Vue.js 3** (Composition API)
- **Vite** (Build tool)
- **Pinia** (State Management)
- **Vue Router** (Routing)
- **Element Plus** (UI Components)
- **Chart.js** (Charts)
- **Axios** (HTTP Client)

## Cấu trúc thư mục

```
FE/
├── public/                     # Static assets
├── src/
│   ├── assets/
│   │   └── styles/             # CSS styles
│   ├── components/
│   │   ├── charts/             # Chart components
│   │   ├── common/             # Header, Sidebar, Footer
│   │   ├── feedback/           # Feedback components
│   │   └── upload/             # Upload components
│   ├── layouts/                # Layout components
│   ├── router/                 # Vue Router config
│   ├── services/               # API services
│   ├── stores/                 # Pinia stores
│   ├── utils/                  # Utilities
│   └── views/
│       ├── admin/              # Admin views
│       ├── feedback/           # Feedback views
│       └── reports/            # Report views
├── index.html
├── package.json
├── vite.config.js
└── jsconfig.json
```

## Cài đặt

```bash
# Cài đặt dependencies
npm install

# Chạy development server
npm run dev

# Build production
npm run build

# Preview production build
npm run preview
```

## Chức năng chính

### 1. Xác thực & Phân quyền
- Đăng nhập/Đăng xuất
- Phân quyền: Admin, Lãnh đạo, Tiếp nhận, Xử lý, Theo dõi

### 2. Quản lý Danh mục
- Quản lý User
- Quản lý Phòng ban
- Quản lý Bác sĩ

### 3. Quản lý Phản ánh
- Nhập phản ánh mới (đa kênh: Hotline, Email, Trực tiếp...)
- Xem danh sách phản ánh (filter, search, pagination)
- Xem chi tiết phản ánh
- Xử lý phản ánh (cập nhật trạng thái, ghi nhận kết quả)
- Upload hình ảnh (tối đa 10 ảnh/phản ánh)

### 4. Báo cáo
- Dashboard tổng quan
- Báo cáo theo Phòng ban
- Báo cáo theo Bác sĩ
- Báo cáo có Hình ảnh
- Export Excel/PDF

## Vai trò người dùng

| Vai trò | Quyền hạn |
|---------|-----------|
| Admin | Full access |
| Lãnh đạo | Xem báo cáo, theo dõi |
| Tiếp nhận | Nhập phản ánh |
| Xử lý | Xử lý phản ánh được giao |
| Theo dõi | Xem phản ánh phòng mình |

## Biến môi trường

Tạo file `.env` với các biến sau (hoặc copy từ `.env.example`):

```env
# Application Configuration
VITE_APP_TITLE=Quản lý Phản ánh - BV Y Hà Nội

# API Configuration
VITE_API_BASE_URL=http://localhost:8080/api

# Upload Configuration
VITE_UPLOAD_MAX_SIZE=5242880
VITE_UPLOAD_MAX_FILES=10

# Demo Mode (for development/testing without backend)
# Set to 'true' to enable demo mode, 'false' to use real API
VITE_DEMO_MODE=false

# Security Configuration
# Secret key for token encryption (use a strong random string in production)
VITE_ENCRYPT_SECRET=your-secret-key-change-in-production
```

### Giải thích các biến môi trường

- **VITE_APP_TITLE**: Tiêu đề ứng dụng
- **VITE_API_BASE_URL**: URL base của API backend
- **VITE_UPLOAD_MAX_SIZE**: Kích thước tối đa của file upload (bytes), mặc định 5MB
- **VITE_UPLOAD_MAX_FILES**: Số lượng file tối đa có thể upload, mặc định 10
- **VITE_DEMO_MODE**: Bật/tắt chế độ demo (true/false). Khi bật, ứng dụng sẽ sử dụng dữ liệu demo thay vì gọi API thật
- **VITE_ENCRYPT_SECRET**: Secret key để mã hóa token trong localStorage. **Quan trọng**: Thay đổi giá trị này trong production!

### Lưu ý

- File `.env` không được commit vào git (đã có trong `.gitignore`)
- Sử dụng `.env.example` làm template
- Sau khi thay đổi `.env`, cần restart dev server

## API Backend

Backend sử dụng Spring Boot 3, chạy trên port 8080. Xem chi tiết tại folder `BE/`.

### API Endpoints Specification

Dưới đây là danh sách đầy đủ các API endpoints mà Frontend sử dụng. Backend cần implement các endpoints này:

#### 1. Authentication (`/api/auth`)

| Method | Endpoint | Mô tả | Request Body | Response |
|--------|----------|-------|--------------|----------|
| `POST` | `/auth/login` | Đăng nhập | `{ username, password }` | `{ token, user }` |
| `POST` | `/auth/logout` | Đăng xuất | - | `{ success: true }` |
| `GET` | `/auth/me` | Lấy thông tin user hiện tại | - | `{ id, username, fullName, email, role, ... }` |
| `PUT` | `/auth/change-password` | Đổi mật khẩu | `{ oldPassword, newPassword }` | `{ success: true }` |
| `POST` | `/auth/forgot-password` | Quên mật khẩu | `{ email }` | `{ success: true, message }` |
| `GET` | `/auth/verify` | Verify token còn valid | - | `{ valid: true }` |
| `POST` | `/auth/refresh` | Refresh access token | `{ refreshToken }` | `{ token, user }` |

**Headers:** Tất cả endpoints (trừ login, forgot-password) yêu cầu `Authorization: Bearer <token>`

---

#### 2. Users Management (`/api/users`)

| Method | Endpoint | Mô tả | Query Params | Request Body | Response |
|--------|----------|-------|--------------|--------------|----------|
| `GET` | `/users` | Danh sách users | `page, size, search, role, status` | - | `{ data: [], total }` |
| `GET` | `/users/:id` | Chi tiết user | - | - | `{ id, username, fullName, ... }` |
| `POST` | `/users` | Tạo user mới | - | `{ username, password, fullName, email, role, departmentId }` | `{ id, ... }` |
| `PUT` | `/users/:id` | Cập nhật user | - | `{ fullName, email, role, departmentId }` | `{ id, ... }` |
| `DELETE` | `/users/:id` | Xóa user | - | - | `{ success: true }` |
| `PUT` | `/users/:id/reset-password` | Reset mật khẩu | - | - | `{ success: true, newPassword }` |
| `PUT` | `/users/:id/toggle-status` | Bật/tắt user | - | - | `{ success: true, status }` |
| `GET` | `/users/handlers` | Danh sách handlers | `departmentId` (optional) | - | `[{ id, fullName, ... }]` |

**Roles:** Chỉ `ADMIN` có quyền truy cập

---

#### 3. Departments Management (`/api/departments`)

| Method | Endpoint | Mô tả | Query Params | Request Body | Response |
|--------|----------|-------|--------------|--------------|----------|
| `GET` | `/departments` | Danh sách phòng ban | `page, size, search, status` | - | `{ data: [], total }` hoặc `[]` |
| `GET` | `/departments/:id` | Chi tiết phòng ban | - | - | `{ id, name, code, ... }` |
| `POST` | `/departments` | Tạo phòng ban mới | - | `{ name, code, description, handlerId }` | `{ id, ... }` |
| `PUT` | `/departments/:id` | Cập nhật phòng ban | - | `{ name, code, description, handlerId }` | `{ id, ... }` |
| `DELETE` | `/departments/:id` | Xóa phòng ban | - | - | `{ success: true }` |

**Lưu ý:** 
- `GET /departments?status=ACTIVE` trả về danh sách phòng ban đang hoạt động (dùng cho dropdown)
- **Roles:** Chỉ `ADMIN` có quyền truy cập

---

#### 4. Doctors Management (`/api/doctors`)

| Method | Endpoint | Mô tả | Query Params | Request Body | Response |
|--------|----------|-------|--------------|--------------|----------|
| `GET` | `/doctors` | Danh sách bác sĩ | `page, size, search, departmentId` | - | `{ data: [], total }` |
| `GET` | `/doctors/:id` | Chi tiết bác sĩ | - | - | `{ id, name, code, departmentId, ... }` |
| `POST` | `/doctors` | Tạo bác sĩ mới | - | `{ name, code, departmentId, specialty, ... }` | `{ id, ... }` |
| `PUT` | `/doctors/:id` | Cập nhật bác sĩ | - | `{ name, code, departmentId, specialty, ... }` | `{ id, ... }` |
| `DELETE` | `/doctors/:id` | Xóa bác sĩ | - | - | `{ success: true }` |

**Lưu ý:** 
- `GET /doctors?departmentId=X` trả về danh sách bác sĩ theo phòng ban
- **Roles:** Chỉ `ADMIN` có quyền truy cập

---

#### 5. Feedbacks Management (`/api/feedbacks`)

| Method | Endpoint | Mô tả | Query Params | Request Body | Response |
|--------|----------|-------|--------------|--------------|----------|
| `GET` | `/feedbacks` | Danh sách phản ánh | `page, size, search, status, level, channel, departmentId, dateFrom, dateTo` | - | `{ data: [], total }` |
| `GET` | `/feedbacks/:id` | Chi tiết phản ánh | - | - | `{ id, code, content, status, level, ... }` |
| `GET` | `/feedbacks/my` | Phản ánh của tôi (HANDLER) | - | - | `[{ id, code, content, status, ... }]` |
| `POST` | `/feedbacks` | Tạo phản ánh mới | - | `{ content, channel, level, departmentId, doctorId, images }` | `{ id, code, ... }` |
| `PUT` | `/feedbacks/:id` | Cập nhật phản ánh | - | `{ content, channel, level, ... }` | `{ id, ... }` |
| `DELETE` | `/feedbacks/:id` | Xóa phản ánh | - | - | `{ success: true }` |
| `PUT` | `/feedbacks/:id/process` | Xử lý phản ánh | - | `{ status, result, note }` | `{ success: true }` |
| `PUT` | `/feedbacks/:id/assign` | Gán handler | - | `{ handlerId }` | `{ success: true }` |
| `PUT` | `/feedbacks/:id/processing` | Cập nhật quá trình xử lý | - | `{ status, note, images }` | `{ success: true }` |
| `GET` | `/feedbacks/:id/history` | Lịch sử xử lý | - | - | `[{ id, status, note, createdAt, ... }]` |

**Lưu ý:**
- **Roles:** 
  - `ADMIN`, `RECEIVER`: Có thể tạo phản ánh mới
  - `HANDLER`: Chỉ xem được phản ánh được giao (`/feedbacks/my`)
  - `ADMIN`, `LEADER`: Xem tất cả phản ánh

---

#### 6. Ratings (`/api/ratings`)

| Method | Endpoint | Mô tả | Query Params | Request Body | Response |
|--------|----------|-------|--------------|--------------|----------|
| `GET` | `/ratings/completed-feedbacks` | Danh sách phản ánh đã hoàn thành | `page, size, hasRating, departmentId, doctorId` | - | `{ data: [], total, stats }` |
| `POST` | `/ratings` | Tạo đánh giá mới | - | `{ feedbackId, doctorId, rating, comment }` | `{ id, ... }` |
| `PUT` | `/ratings/:id` | Cập nhật đánh giá | - | `{ rating, comment }` | `{ id, ... }` |
| `GET` | `/ratings/by-feedback/:feedbackId` | Đánh giá theo phản ánh | - | - | `{ id, rating, comment, ... }` hoặc `null` |
| `GET` | `/ratings/doctor/:doctorId/average` | Điểm trung bình của bác sĩ | - | - | `{ avgRating, totalRatings }` |
| `GET` | `/ratings/doctor/:doctorId` | Tất cả đánh giá của bác sĩ | `page, size` | - | `{ data: [], total }` |
| `GET` | `/ratings/statistics` | Thống kê đánh giá | `dateFrom, dateTo, departmentId` | - | `{ totalRatings, avgRating, ratingDistribution, topDoctors }` |

**Lưu ý:**
- `rating`: số từ 1-5
- **Roles:** `ADMIN`, `LEADER`, `HANDLER` có quyền đánh giá

---

#### 7. Reports (`/api/reports`)

| Method | Endpoint | Mô tả | Query Params | Response |
|--------|----------|-------|--------------|----------|
| `GET` | `/reports/dashboard` | Thống kê dashboard | - | `{ stats: { total, pending, completed, ... }, monthlyStats: [], departmentStats: [], recentFeedbacks: [] }` |
| `GET` | `/reports/by-department` | Báo cáo theo phòng ban | `dateFrom, dateTo, departmentId` | `{ data: [{ departmentId, departmentName, count, ... }], summary: { total, ... } }` |
| `GET` | `/reports/by-doctor` | Báo cáo theo bác sĩ | `dateFrom, dateTo, departmentId` | `{ data: [{ doctorId, doctorName, count, ... }], total }` |
| `GET` | `/reports/with-images` | Phản ánh có hình ảnh | `dateFrom, dateTo, departmentId` | `[{ id, code, images: [], ... }]` |
| `GET` | `/reports/monthly-stats` | Thống kê theo tháng | `year` | `[{ month, count }]` |
| `GET` | `/reports/export-excel` | Export Excel | `dateFrom, dateTo, type, ...` | `Blob` (file download) |
| `GET` | `/reports/export-pdf` | Export PDF | `dateFrom, dateTo, type, ...` | `Blob` (file download) |

**Lưu ý:**
- **Roles:** `ADMIN`, `LEADER` có quyền xem báo cáo
- Export endpoints trả về `responseType: 'blob'`

---

#### 8. Upload (`/api/upload`)

| Method | Endpoint | Mô tả | Request Body | Response |
|--------|----------|-------|--------------|----------|
| `POST` | `/upload/feedback-images` | Upload hình ảnh phản ánh | `FormData: { files: File[], feedbackId }` | `{ images: [{ id, filename, url }] }` |
| `POST` | `/upload/process-images` | Upload hình ảnh xử lý | `FormData: { files: File[], feedbackId }` | `{ images: [{ id, filename, url }] }` |
| `DELETE` | `/upload/images/:imageId` | Xóa hình ảnh | - | `{ success: true }` |
| `GET` | `/upload/images/:filename` | Lấy hình ảnh | - | `Image file` |

**Lưu ý:**
- Content-Type: `multipart/form-data`
- Max file size: 5MB (configurable via `VITE_UPLOAD_MAX_SIZE`)
- Max files: 10 (configurable via `VITE_UPLOAD_MAX_FILES`)
- Accept types: `image/jpeg`, `image/png`, `image/webp`

---

#### 9. Notifications (`/api/notifications`)

| Method | Endpoint | Mô tả | Request Body | Response |
|--------|----------|-------|--------------|----------|
| `GET` | `/notifications` | Danh sách thông báo | - | `[{ id, type, title, message, read, createdAt }]` |
| `POST` | `/notifications/mark-all-read` | Đánh dấu tất cả đã đọc | - | `{ success: true }` |
| `PUT` | `/notifications/:id/read` | Đánh dấu đã đọc | - | `{ success: true }` |

**Lưu ý:**
- Notification types: `feedback`, `assigned`, `completed`
- Frontend tự động refresh mỗi 30 giây

---

### API Response Format

#### Success Response
```json
{
  "data": { ... },           // Single object
  "data": [ ... ],           // Array of objects
  "total": 100,              // Total count (for pagination)
  "success": true,           // Success flag
  "message": "..."           // Optional message
}
```

#### Error Response
```json
{
  "message": "Error message",
  "code": "ERROR_CODE",      // Optional error code
  "errors": { ... }          // Optional validation errors
}
```

#### HTTP Status Codes
- `200` - Success
- `201` - Created
- `400` - Bad Request
- `401` - Unauthorized (token invalid/expired)
- `403` - Forbidden (insufficient permissions)
- `404` - Not Found
- `422` - Validation Error
- `500` - Internal Server Error

---

### Authentication

Tất cả API requests (trừ `/auth/login` và `/auth/forgot-password`) yêu cầu header:
```
Authorization: Bearer <access_token>
```

Token được lưu trong localStorage và tự động được thêm vào mỗi request qua Axios interceptor.

Khi nhận được `401 Unauthorized`, Frontend sẽ tự động:
1. Xóa token và user khỏi localStorage
2. Redirect về `/login`

---

### Pagination

Các endpoints có pagination sử dụng query parameters:
- `page`: Số trang (bắt đầu từ 1)
- `size`: Số items mỗi trang (default: 20)

Response format:
```json
{
  "data": [ ... ],
  "total": 100,
  "page": 1,
  "size": 20,
  "totalPages": 5
}
```

---

### Filtering & Searching

Các endpoints hỗ trợ filtering qua query parameters:
- `search`: Tìm kiếm text (thường search trong nhiều fields)
- `status`: Filter theo status (e.g., `NEW`, `PROCESSING`, `COMPLETED`)
- `level`: Filter theo level (e.g., `CRITICAL`, `HIGH`, `MEDIUM`, `LOW`)
- `channel`: Filter theo channel (e.g., `HOTLINE`, `EMAIL`, `DIRECT`)
- `departmentId`: Filter theo phòng ban
- `dateFrom`, `dateTo`: Filter theo khoảng thời gian (format: `YYYY-MM-DD`)

---

### Constants Reference

#### Feedback Status
- `NEW`: Chưa xử lý
- `PROCESSING`: Đang xử lý
- `COMPLETED`: Hoàn thành

#### Feedback Level
- `CRITICAL`: Khẩn cấp
- `HIGH`: Cao
- `MEDIUM`: Trung bình
- `LOW`: Thấp

#### Feedback Channel
- `HOTLINE`: Hotline
- `EMAIL`: Email
- `DIRECT`: Trực tiếp
- `ZALO`: Zalo
- `FACEBOOK`: Facebook
- `OTHER`: Khác

#### User Roles
- `ADMIN`: Quản trị viên
- `LEADER`: Lãnh đạo
- `RECEIVER`: Người tiếp nhận
- `HANDLER`: Người xử lý
- `VIEWER`: Người theo dõi

#### User Status
- `ACTIVE`: Hoạt động
- `INACTIVE`: Ngừng hoạt động

## Liên hệ

- **Lead BA/BE:** Nguyễn Thanh Tuấn
- **Client:** Bệnh viện Y Hà Nội

---

**Created:** 2025-11-27
**Version:** 1.0.0

