# Database Setup Guide

Hướng dẫn tạo và cấu hình database cho hệ thống Quản lý Phản ánh.

## 📋 Yêu cầu

- PostgreSQL 14+ (khuyến nghị PostgreSQL 16)
- Docker Desktop (nếu dùng Docker)

## 🚀 Cách 1: Tự động tạo Schema (Khuyến nghị)

Spring Boot sẽ tự động tạo database schema từ JPA entities khi chạy ứng dụng lần đầu.

### Cấu hình trong `application.yml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update # Tự động tạo/cập nhật schema
```

### Các bước:

1. **Tạo database** (nếu chưa có):
   ```sql
   CREATE DATABASE feedback_db;
   ```

2. **Cấu hình connection** trong `application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://localhost:5432/feedback_db
       username: postgres
       password: postgres
   ```

3. **Chạy ứng dụng Spring Boot**:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. **Schema sẽ được tạo tự động** từ các JPA entities:
   - `users`
   - `departments`
   - `doctors`
   - `feedbacks`
   - `feedback_history`
   - `feedback_images`
   - `ratings`
   - `notifications`

5. **Demo data sẽ được tạo** bởi `DataInitializer` (nếu chưa có):
   - Users (admin, leader, receiver, handler1-5)
   - Departments (5 phòng ban)
   - Doctors (10 bác sĩ)
   - Feedbacks (10 phản ánh mẫu)

## 🐳 Cách 2: Dùng Docker (Khuyến nghị cho Development)

### Tạo và chạy PostgreSQL container:

```powershell
# Tạo container PostgreSQL
docker run -d `
  --name feedback-postgres `
  -p 5432:5432 `
  -e POSTGRES_PASSWORD=postgres `
  -e POSTGRES_DB=feedback_db `
  -v feedback_db_data:/var/lib/postgresql/data `
  postgres:16

# Kiểm tra container đang chạy
docker ps --filter "name=feedback-postgres"
```

### Kết nối database:

```powershell
# Kết nối vào PostgreSQL
docker exec -it feedback-postgres psql -U postgres -d feedback_db
```

### Schema sẽ được tạo tự động khi chạy Spring Boot.

## 📝 Cách 3: Tạo Schema thủ công (Cho Production)

Nếu muốn tạo schema thủ công, có thể export từ database sau khi chạy lần đầu:

```powershell
# Export schema
docker exec feedback-postgres pg_dump -U postgres -d feedback_db --schema-only > schema.sql

# Hoặc export cả data
docker exec feedback-postgres pg_dump -U postgres -d feedback_db > full_database.sql
```


## 📊 Database Schema Overview

Xem chi tiết đầy đủ tất cả các bảng và cột tại: [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md)

### Tables:

1. **users** - Người dùng hệ thống (10 cột)
2. **departments** - Phòng ban (10 cột)
3. **doctors** - Bác sĩ (10 cột)
4. **feedbacks** - Phản ánh (14 cột)
5. **feedback_history** - Lịch sử xử lý phản ánh (8 cột)
6. **feedback_images** - Hình ảnh đính kèm (6 cột)
7. **ratings** - Đánh giá bác sĩ (8 cột)
8. **notifications** - Thông báo (9 cột)

### Relationships:

- `feedbacks.department_id` → `departments.id`
- `feedbacks.doctor_id` → `doctors.id`
- `feedbacks.handler_id` → `users.id`
- `feedbacks.receiver_id` → `users.id`
- `feedback_history.feedback_id` → `feedbacks.id`
- `feedback_history.created_by` → `users.id`
- `feedback_images.feedback_id` → `feedbacks.id`
- `ratings.feedback_id` → `feedbacks.id`
- `ratings.doctor_id` → `doctors.id`
- `ratings.user_id` → `users.id`
- `notifications.user_id` → `users.id`
- `notifications.feedback_id` → `feedbacks.id`
- `notifications.rating_id` → `ratings.id`
- `users.department_id` → `departments.id`
- `doctors.department_id` → `departments.id`
- `departments.handler_id` → `users.id`
- `departments.manager_id` → `users.id`

## 🔧 Cấu hình Production

### Thay đổi `ddl-auto` trong `application.yml`:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate # Chỉ validate, không tự động tạo/cập nhật
```

### Backup database:

```powershell
# Backup
docker exec feedback-postgres pg_dump -U postgres -d feedback_db > backup_$(Get-Date -Format "yyyyMMdd_HHmmss").sql

# Restore
docker exec -i feedback-postgres psql -U postgres -d feedback_db < backup_20251208_120000.sql
```

## 🐛 Troubleshooting

### Lỗi "database does not exist":

```sql
CREATE DATABASE feedback_db;
```

### Lỗi "permission denied":

```sql
GRANT ALL PRIVILEGES ON DATABASE feedback_db TO postgres;
```

### Xem schema hiện tại:

```sql
\dt -- List tables
\d+ table_name -- Describe table
```

### Reset database (XÓA TẤT CẢ DỮ LIỆU):

```powershell
# Xóa container và volume
docker stop feedback-postgres
docker rm feedback-postgres
docker volume rm feedback_db_data

# Tạo lại
docker run -d `
  --name feedback-postgres `
  -p 5432:5432 `
  -e POSTGRES_PASSWORD=postgres `
  -e POSTGRES_DB=feedback_db `
  -v feedback_db_data:/var/lib/postgresql/data `
  postgres:16
```

## 📚 Tham khảo

- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Hibernate DDL Auto](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#configurations-hbmddl)

