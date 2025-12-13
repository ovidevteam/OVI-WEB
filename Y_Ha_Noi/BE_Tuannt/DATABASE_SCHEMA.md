# Database Schema - Chi tiết các Bảng và Cột

Danh sách đầy đủ tất cả các bảng và cột trong database `feedback_db`.

## 📊 Tổng quan

Database có **8 bảng chính**:
1. `users` - Người dùng hệ thống
2. `departments` - Phòng ban
3. `doctors` - Bác sĩ
4. `feedbacks` - Phản ánh
5. `feedback_history` - Lịch sử xử lý phản ánh
6. `feedback_images` - Hình ảnh đính kèm
7. `ratings` - Đánh giá bác sĩ
8. `notifications` - Thông báo

---

## 1. Bảng `users`

**Mô tả:** Lưu thông tin người dùng hệ thống (admin, leader, receiver, handler, viewer)

| Tên cột | Kiểu dữ liệu | Nullable | Unique | Mô tả |
|---------|--------------|----------|--------|-------|
| `id` | BIGSERIAL | ❌ | ✅ | Primary key, auto increment |
| `username` | VARCHAR(50) | ❌ | ✅ | Tên đăng nhập (unique) |
| `password` | VARCHAR | ❌ | ❌ | Mật khẩu (đã hash) |
| `full_name` | VARCHAR(100) | ✅ | ❌ | Họ và tên đầy đủ |
| `email` | VARCHAR(100) | ✅ | ✅ | Email (unique) |
| `phone` | VARCHAR(20) | ✅ | ❌ | Số điện thoại |
| `role` | VARCHAR(20) | ❌ | ❌ | Vai trò: ADMIN, LEADER, RECEIVER, HANDLER, VIEWER |
| `department_id` | BIGINT | ✅ | ❌ | Foreign key → `departments.id` |
| `status` | VARCHAR(20) | ❌ | ❌ | Trạng thái: ACTIVE, INACTIVE (default: ACTIVE) |
| `created_at` | TIMESTAMP | ❌ | ❌ | Thời gian tạo (auto) |
| `updated_at` | TIMESTAMP | ✅ | ❌ | Thời gian cập nhật (auto) |

**Indexes:**
- Primary key: `id`
- Unique: `username`, `email`

---

## 2. Bảng `departments`

**Mô tả:** Lưu thông tin các phòng ban trong bệnh viện

| Tên cột | Kiểu dữ liệu | Nullable | Unique | Mô tả |
|---------|--------------|----------|--------|-------|
| `id` | BIGSERIAL | ❌ | ✅ | Primary key, auto increment |
| `code` | VARCHAR(50) | ❌ | ✅ | Mã phòng ban (unique, ví dụ: PB-001) |
| `name` | VARCHAR(100) | ❌ | ❌ | Tên phòng ban |
| `description` | TEXT | ✅ | ❌ | Mô tả phòng ban |
| `handler_id` | BIGINT | ✅ | ❌ | ID người xử lý mặc định → `users.id` |
| `manager_id` | BIGINT | ✅ | ❌ | ID quản lý phòng ban → `users.id` |
| `notification_email` | VARCHAR(100) | ✅ | ❌ | Email nhận thông báo |
| `status` | VARCHAR(20) | ❌ | ❌ | Trạng thái: ACTIVE, INACTIVE (default: ACTIVE) |
| `created_at` | TIMESTAMP | ❌ | ❌ | Thời gian tạo (auto) |
| `updated_at` | TIMESTAMP | ✅ | ❌ | Thời gian cập nhật (auto) |

**Indexes:**
- Primary key: `id`
- Unique: `code`

---

## 3. Bảng `doctors`

**Mô tả:** Lưu thông tin các bác sĩ

| Tên cột | Kiểu dữ liệu | Nullable | Unique | Mô tả |
|---------|--------------|----------|--------|-------|
| `id` | BIGSERIAL | ❌ | ✅ | Primary key, auto increment |
| `code` | VARCHAR(50) | ❌ | ✅ | Mã bác sĩ (unique, ví dụ: BS-001) |
| `full_name` | VARCHAR(100) | ❌ | ❌ | Họ và tên đầy đủ |
| `specialty` | VARCHAR(100) | ✅ | ❌ | Chuyên khoa (ví dụ: Nội khoa, Ngoại khoa) |
| `department_id` | BIGINT | ❌ | ❌ | Foreign key → `departments.id` |
| `email` | VARCHAR(100) | ✅ | ❌ | Email |
| `phone` | VARCHAR(20) | ✅ | ❌ | Số điện thoại |
| `status` | VARCHAR(20) | ❌ | ❌ | Trạng thái: ACTIVE, INACTIVE (default: ACTIVE) |
| `created_at` | TIMESTAMP | ❌ | ❌ | Thời gian tạo (auto) |
| `updated_at` | TIMESTAMP | ✅ | ❌ | Thời gian cập nhật (auto) |

**Indexes:**
- Primary key: `id`
- Unique: `code`

---

## 4. Bảng `feedbacks`

**Mô tả:** Lưu thông tin các phản ánh từ bệnh nhân/người dùng

| Tên cột | Kiểu dữ liệu | Nullable | Unique | Mô tả |
|---------|--------------|----------|--------|-------|
| `id` | BIGSERIAL | ❌ | ✅ | Primary key, auto increment |
| `code` | VARCHAR(50) | ❌ | ✅ | Mã phản ánh (unique, ví dụ: PA-20251208-001) |
| `content` | TEXT | ❌ | ❌ | Nội dung phản ánh |
| `channel` | VARCHAR(20) | ❌ | ❌ | Kênh tiếp nhận: PHONE, EMAIL, WEBSITE, DIRECT |
| `level` | VARCHAR(20) | ❌ | ❌ | Mức độ: LOW, MEDIUM, HIGH |
| `status` | VARCHAR(20) | ❌ | ❌ | Trạng thái: NEW, ASSIGNED, PROCESSING, COMPLETED, CANCELLED (default: NEW) |
| `department_id` | BIGINT | ✅ | ❌ | Foreign key → `departments.id` |
| `doctor_id` | BIGINT | ✅ | ❌ | Foreign key → `doctors.id` |
| `handler_id` | BIGINT | ✅ | ❌ | Foreign key → `users.id` (người xử lý) |
| `receiver_id` | BIGINT | ✅ | ❌ | Foreign key → `users.id` (người tiếp nhận) |
| `received_date` | DATE | ❌ | ❌ | Ngày nhận phản ánh (auto: today nếu null) |
| `completed_date` | DATE | ✅ | ❌ | Ngày hoàn thành xử lý |
| `created_at` | TIMESTAMP | ❌ | ❌ | Thời gian tạo (auto) |
| `updated_at` | TIMESTAMP | ✅ | ❌ | Thời gian cập nhật (auto) |

**Indexes:**
- Primary key: `id`
- Unique: `code`

---

## 5. Bảng `feedback_history`

**Mô tả:** Lưu lịch sử xử lý phản ánh (timeline)

| Tên cột | Kiểu dữ liệu | Nullable | Unique | Mô tả |
|---------|--------------|----------|--------|-------|
| `id` | BIGSERIAL | ❌ | ✅ | Primary key, auto increment |
| `feedback_id` | BIGINT | ❌ | ❌ | Foreign key → `feedbacks.id` |
| `status` | VARCHAR(20) | ❌ | ❌ | Trạng thái tại thời điểm này: NEW, ASSIGNED, PROCESSING, COMPLETED, CANCELLED |
| `content` | TEXT | ✅ | ❌ | Nội dung xử lý (mô tả chi tiết) |
| `note` | TEXT | ✅ | ❌ | Ghi chú thêm |
| `image_ids` | TEXT | ✅ | ❌ | JSON array chứa IDs của images (ví dụ: "[1,2,3]") |
| `created_by` | BIGINT | ❌ | ❌ | Foreign key → `users.id` (người tạo history entry) |
| `created_at` | TIMESTAMP | ❌ | ❌ | Thời gian tạo (auto) |

**Indexes:**
- Primary key: `id`

---

## 6. Bảng `feedback_images`

**Mô tả:** Lưu thông tin hình ảnh đính kèm phản ánh hoặc quá trình xử lý

| Tên cột | Kiểu dữ liệu | Nullable | Unique | Mô tả |
|---------|--------------|----------|--------|-------|
| `id` | BIGSERIAL | ❌ | ✅ | Primary key, auto increment |
| `feedback_id` | BIGINT | ❌ | ❌ | Foreign key → `feedbacks.id` |
| `filename` | VARCHAR(255) | ❌ | ❌ | Tên file (UUID format) |
| `file_path` | VARCHAR(500) | ❌ | ❌ | Đường dẫn đầy đủ đến file |
| `image_type` | VARCHAR(20) | ❌ | ❌ | Loại ảnh: FEEDBACK (ảnh từ phản ánh), PROCESS (ảnh từ quá trình xử lý) |
| `created_at` | TIMESTAMP | ❌ | ❌ | Thời gian tạo (auto) |

**Indexes:**
- Primary key: `id`

---

## 7. Bảng `ratings`

**Mô tả:** Lưu đánh giá của người dùng về bác sĩ sau khi xử lý phản ánh

| Tên cột | Kiểu dữ liệu | Nullable | Unique | Mô tả |
|---------|--------------|----------|--------|-------|
| `id` | BIGSERIAL | ❌ | ✅ | Primary key, auto increment |
| `feedback_id` | BIGINT | ❌ | ❌ | Foreign key → `feedbacks.id` |
| `user_id` | BIGINT | ❌ | ❌ | Foreign key → `users.id` (người đánh giá) |
| `doctor_id` | BIGINT | ✅ | ❌ | Foreign key → `doctors.id` (bác sĩ được đánh giá) |
| `rating` | INTEGER | ❌ | ❌ | Điểm đánh giá (1-5 sao) |
| `comment` | TEXT | ✅ | ❌ | Nhận xét/bình luận |
| `created_at` | TIMESTAMP | ❌ | ❌ | Thời gian tạo (auto) |
| `updated_at` | TIMESTAMP | ✅ | ❌ | Thời gian cập nhật (auto) |

**Indexes:**
- Primary key: `id`
- Unique constraint: `(feedback_id, user_id)` - Mỗi user chỉ đánh giá 1 lần cho mỗi feedback

---

## 8. Bảng `notifications`

**Mô tả:** Lưu thông báo cho người dùng

| Tên cột | Kiểu dữ liệu | Nullable | Unique | Mô tả |
|---------|--------------|----------|--------|-------|
| `id` | BIGSERIAL | ❌ | ✅ | Primary key, auto increment |
| `user_id` | BIGINT | ❌ | ❌ | Foreign key → `users.id` (người nhận thông báo) |
| `type` | VARCHAR(20) | ❌ | ❌ | Loại thông báo: FEEDBACK, ASSIGNED, COMPLETED, RATING |
| `title` | VARCHAR(200) | ❌ | ❌ | Tiêu đề thông báo |
| `message` | TEXT | ❌ | ❌ | Nội dung thông báo |
| `read` | BOOLEAN | ❌ | ❌ | Đã đọc chưa (default: false) |
| `feedback_id` | BIGINT | ✅ | ❌ | Foreign key → `feedbacks.id` (nếu liên quan đến feedback) |
| `rating_id` | BIGINT | ✅ | ❌ | Foreign key → `ratings.id` (nếu liên quan đến rating) |
| `created_at` | TIMESTAMP | ❌ | ❌ | Thời gian tạo (auto) |

**Indexes:**
- Primary key: `id`

---

## 🔗 Foreign Key Relationships

### `feedbacks` table:
- `department_id` → `departments.id`
- `doctor_id` → `doctors.id`
- `handler_id` → `users.id`
- `receiver_id` → `users.id`

### `feedback_history` table:
- `feedback_id` → `feedbacks.id`
- `created_by` → `users.id`

### `feedback_images` table:
- `feedback_id` → `feedbacks.id`

### `ratings` table:
- `feedback_id` → `feedbacks.id`
- `user_id` → `users.id`
- `doctor_id` → `doctors.id`

### `notifications` table:
- `user_id` → `users.id`
- `feedback_id` → `feedbacks.id`
- `rating_id` → `ratings.id`

### `users` table:
- `department_id` → `departments.id`

### `doctors` table:
- `department_id` → `departments.id`

### `departments` table:
- `handler_id` → `users.id`
- `manager_id` → `users.id`

---

## 📝 Ghi chú

1. **Auto-generated fields:**
   - `id`: Tự động tăng (BIGSERIAL)
   - `created_at`: Tự động set khi tạo record
   - `updated_at`: Tự động cập nhật khi modify record

2. **Enum values:**
   - `users.role`: ADMIN, LEADER, RECEIVER, HANDLER, VIEWER
   - `users.status`: ACTIVE, INACTIVE
   - `departments.status`: ACTIVE, INACTIVE
   - `doctors.status`: ACTIVE, INACTIVE
   - `feedbacks.channel`: PHONE, EMAIL, WEBSITE, DIRECT
   - `feedbacks.level`: LOW, MEDIUM, HIGH
   - `feedbacks.status`: NEW, ASSIGNED, PROCESSING, COMPLETED, CANCELLED
   - `feedback_images.image_type`: FEEDBACK, PROCESS
   - `notifications.type`: FEEDBACK, ASSIGNED, COMPLETED, RATING

3. **JSON fields:**
   - `feedback_history.image_ids`: Lưu dưới dạng JSON string, ví dụ: `"[1,2,3]"`

4. **Unique constraints:**
   - `users.username` - unique
   - `users.email` - unique
   - `departments.code` - unique
   - `doctors.code` - unique
   - `feedbacks.code` - unique
   - `ratings(feedback_id, user_id)` - unique (composite)

---

## 🔍 Query Examples

### Xem tất cả bảng:
```sql
\dt
```

### Xem chi tiết một bảng:
```sql
\d+ table_name
```

### Xem tất cả foreign keys:
```sql
SELECT
    tc.table_name, 
    kcu.column_name, 
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name 
FROM 
    information_schema.table_constraints AS tc 
    JOIN information_schema.key_column_usage AS kcu
      ON tc.constraint_name = kcu.constraint_name
    JOIN information_schema.constraint_column_usage AS ccu
      ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY';
```

### Đếm số records trong mỗi bảng:
```sql
SELECT 
    'users' as table_name, COUNT(*) as count FROM users
UNION ALL
SELECT 'departments', COUNT(*) FROM departments
UNION ALL
SELECT 'doctors', COUNT(*) FROM doctors
UNION ALL
SELECT 'feedbacks', COUNT(*) FROM feedbacks
UNION ALL
SELECT 'feedback_history', COUNT(*) FROM feedback_history
UNION ALL
SELECT 'feedback_images', COUNT(*) FROM feedback_images
UNION ALL
SELECT 'ratings', COUNT(*) FROM ratings
UNION ALL
SELECT 'notifications', COUNT(*) FROM notifications;
```

