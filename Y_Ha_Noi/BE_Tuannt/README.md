# Backend - Hệ thống Quản lý Phản ánh

**Version:** 1.4.2

> 📝 Xem [CHANGELOG.md](./CHANGELOG.md) để biết lịch sử thay đổi và các tính năng mới.

## 🚀 Quick Start

### Yêu cầu hệ thống
- **JDK 17+** (kiểm tra: `java -version`)
- **Maven** hoặc dùng `mvnw.cmd` (Maven Wrapper có sẵn trong project)
- **Docker Desktop** (để chạy PostgreSQL)

### Các lệnh chạy BE

#### 1. Kiểm tra Docker PostgreSQL đã chạy
```powershell
# Kiểm tra container đang chạy
docker ps --filter "name=feedback-postgres"

# Nếu chưa chạy, khởi động container
docker start feedback-postgres

# Nếu chưa có container, tạo mới
docker run -d `
  --name feedback-postgres `
  -p 5432:5432 `
  -e POSTGRES_PASSWORD=postgres `
  -e POSTGRES_DB=feedback_db `
  -v feedback_db_data:/var/lib/postgresql/data `
  postgres:16
```

#### 2. Build project (lần đầu hoặc sau khi thay đổi code)
```powershell
# Cách 1: Dùng Maven Wrapper (khuyến nghị - không cần cài Maven)
.\mvnw.cmd clean install

# Cách 2: Dùng Maven (nếu đã cài Maven)
mvn clean install

# Chỉ compile không chạy test (nhanh hơn)
.\mvnw.cmd clean compile -DskipTests
```

#### 3. Chạy ứng dụng Spring Boot
```powershell
# Cách 1: Dùng Maven Wrapper (khuyến nghị)
.\mvnw.cmd spring-boot:run

# Cách 2: Dùng Maven
mvn spring-boot:run

# Cách 3: Chạy từ file JAR (sau khi build)
java -jar target/feedback-management-1.4.0.jar
```

#### 4. Kiểm tra ứng dụng đã chạy
```powershell
# Kiểm tra API health
Invoke-WebRequest -Uri "http://localhost:8080/api/auth/verify" -Method Get

# Hoặc mở browser
start http://localhost:8080/api
```

**Ứng dụng sẽ chạy tại:** `http://localhost:8080/api`

### Lệnh nhanh (PowerShell)

```powershell
# 1. Khởi động PostgreSQL
docker start feedback-postgres

# 2. Chạy BE (trong thư mục BE)
cd "F:\OVI BE\Y_Ha_Noi\BE"
.\mvnw.cmd spring-boot:run
```

### Troubleshooting

```powershell
# Nếu lỗi "port 8080 already in use"
netstat -ano | findstr :8080
# Tìm PID và kill process
Stop-Process -Id <PID> -Force

# Nếu lỗi "cannot connect to database"
docker ps --filter "name=feedback-postgres"
docker start feedback-postgres

# Xem logs của BE
Get-Content "logs\feedback-management.log" -Tail 50

# Xem logs của PostgreSQL
docker logs feedback-postgres
```

## 📁 Cấu trúc Project

```
src/
├── main/
│   ├── java/com/bvyhanoi/feedback/
│   │   ├── FeedbackApplication.java
│   │   ├── config/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── security/
│   │   └── exception/
│   └── resources/
│       └── application.yml
└── test/
    └── java/
```

## 🔧 Cấu hình

### Database
- **Host:** localhost:5432
- **Database:** feedback_db
- **Username:** postgres
- **Password:** postgres

### API Base URL
- **Development:** http://localhost:8080/api
- **Frontend:** http://localhost:5173

## 📝 API Documentation

Xem chi tiết tại: `FE/README.md` - Phần "API Backend"

## 🐳 Docker Commands

```bash
# Start PostgreSQL
docker start feedback-postgres

# Stop PostgreSQL
docker stop feedback-postgres

# View logs
docker logs feedback-postgres

# Connect to database
docker exec -it feedback-postgres psql -U postgres -d feedback_db
```

## 📊 Database Setup

Xem chi tiết hướng dẫn tạo database tại: [DATABASE_SETUP.md](./DATABASE_SETUP.md)

**Tóm tắt:**
- Spring Boot tự động tạo schema từ JPA entities (với `ddl-auto: update`)
- Demo data được tạo tự động bởi `DataInitializer` khi chạy lần đầu
- Không cần chạy SQL scripts thủ công (trừ khi cần migration)

