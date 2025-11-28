# Kiến trúc Kỹ thuật - Hệ thống Quản lý Phản ánh

## 🏗️ Kiến trúc Tổng quan

```
┌─────────────────────────────────────────────────────┐
│                  CLIENT BROWSER                      │
│              Vue.js 3 (Composition API)             │
│          + Element Plus + Chart.js/ECharts          │
└────────────────────┬────────────────────────────────┘
                     │ HTTP/REST API (JSON)
                     │ Axios
                     ▼
┌─────────────────────────────────────────────────────┐
│                   NGINX (Reverse Proxy)             │
│              Port 80/443 (HTTPS with SSL)           │
└────────────────────┬────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│            SPRING BOOT 3 APPLICATION                │
│                   Port 8080                         │
│  ┌──────────────────────────────────────────────┐  │
│  │  Controllers (REST API Endpoints)            │  │
│  │  - AuthController                            │  │
│  │  - FeedbackController                        │  │
│  │  - DepartmentController                      │  │
│  │  - ReportController                          │  │
│  └──────────────┬───────────────────────────────┘  │
│                 │                                   │
│  ┌──────────────▼───────────────────────────────┐  │
│  │  Services (Business Logic)                   │  │
│  │  - FeedbackService                           │  │
│  │  - EmailService (JavaMail)                   │  │
│  │  - FileUploadService                         │  │
│  └──────────────┬───────────────────────────────┘  │
│                 │                                   │
│  ┌──────────────▼───────────────────────────────┐  │
│  │  Repositories (Spring Data JPA)              │  │
│  │  - FeedbackRepository                        │  │
│  │  - UserRepository                            │  │
│  └──────────────┬───────────────────────────────┘  │
└─────────────────┼───────────────────────────────────┘
                  │ JDBC
                  ▼
┌─────────────────────────────────────────────────────┐
│              MySQL 8 / PostgreSQL                   │
│                   Port 3306/5432                    │
│  - Database: hospital_feedback                     │
│  - Tables: users, feedbacks, departments...        │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              FILE STORAGE (Local)                   │
│         /var/www/uploads/feedbacks/                 │
│         /var/www/uploads/process-results/           │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              EMAIL SERVER (SMTP)                    │
│         Gmail SMTP: smtp.gmail.com:587              │
│         App Password (2FA required)                 │
└─────────────────────────────────────────────────────┘
```

---

## 📁 Cấu trúc Project

### Frontend (Vue.js)

```
y-hanoi-feedback-frontend/
├── public/
│   ├── index.html
│   └── favicon.ico
├── src/
│   ├── main.js                         # Entry point
│   ├── App.vue                         # Root component
│   ├── router/
│   │   └── index.js                    # Vue Router config
│   ├── store/
│   │   ├── index.js                    # Pinia store
│   │   ├── auth.js                     # Auth store
│   │   └── feedback.js                 # Feedback store
│   ├── views/                          # Pages
│   │   ├── Login.vue
│   │   ├── Dashboard.vue
│   │   ├── FeedbackList.vue
│   │   ├── FeedbackCreate.vue
│   │   ├── FeedbackDetail.vue
│   │   ├── MyFeedbacks.vue
│   │   ├── DepartmentManagement.vue
│   │   ├── DoctorManagement.vue
│   │   ├── UserManagement.vue
│   │   ├── ReportByDepartment.vue
│   │   └── ReportByDoctor.vue
│   ├── components/                     # Reusable components
│   │   ├── common/
│   │   │   ├── Header.vue
│   │   │   ├── Sidebar.vue
│   │   │   └── Footer.vue
│   │   ├── feedback/
│   │   │   ├── FeedbackCard.vue
│   │   │   ├── FeedbackForm.vue
│   │   │   ├── FeedbackTimeline.vue
│   │   │   └── ImageGallery.vue
│   │   ├── charts/
│   │   │   ├── LineChart.vue
│   │   │   └── BarChart.vue
│   │   └── upload/
│   │       └── ImageUpload.vue
│   ├── services/                       # API services
│   │   ├── api.js                      # Axios instance
│   │   ├── authService.js
│   │   ├── feedbackService.js
│   │   ├── departmentService.js
│   │   ├── doctorService.js
│   │   └── reportService.js
│   ├── utils/
│   │   ├── constants.js
│   │   ├── validators.js
│   │   └── helpers.js
│   ├── assets/
│   │   ├── styles/
│   │   │   ├── main.css
│   │   │   └── variables.css
│   │   └── images/
│   └── layouts/
│       ├── DefaultLayout.vue
│       └── AuthLayout.vue
├── .env                                # Environment variables
├── .env.production
├── package.json
├── vite.config.js                      # Vite config
└── README.md
```

### Backend (Spring Boot)

```
y-hanoi-feedback-backend/
├── src/
│   ├── main/
│   │   ├── java/com/ovi/hospitalfeedback/
│   │   │   ├── HospitalFeedbackApplication.java
│   │   │   ├── config/
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── CorsConfig.java
│   │   │   │   ├── EmailConfig.java
│   │   │   │   └── FileUploadConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── FeedbackController.java
│   │   │   │   ├── DepartmentController.java
│   │   │   │   ├── DoctorController.java
│   │   │   │   ├── UserController.java
│   │   │   │   ├── ReportController.java
│   │   │   │   └── FileUploadController.java
│   │   │   ├── service/
│   │   │   │   ├── FeedbackService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── FileUploadService.java
│   │   │   │   ├── UserService.java
│   │   │   │   └── ReportService.java
│   │   │   ├── repository/
│   │   │   │   ├── FeedbackRepository.java
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── DepartmentRepository.java
│   │   │   │   ├── DoctorRepository.java
│   │   │   │   ├── FeedbackImageRepository.java
│   │   │   │   └── FeedbackLogRepository.java
│   │   │   ├── entity/
│   │   │   │   ├── User.java
│   │   │   │   ├── Department.java
│   │   │   │   ├── Doctor.java
│   │   │   │   ├── Feedback.java
│   │   │   │   ├── FeedbackImage.java
│   │   │   │   └── FeedbackLog.java
│   │   │   ├── dto/
│   │   │   │   ├── FeedbackDTO.java
│   │   │   │   ├── FeedbackCreateDTO.java
│   │   │   │   ├── FeedbackUpdateDTO.java
│   │   │   │   └── ReportDTO.java
│   │   │   ├── security/
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   └── util/
│   │   │       ├── DateUtil.java
│   │   │       └── ImageUtil.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-dev.properties
│   │       ├── application-prod.properties
│   │       └── templates/
│   │           ├── email-new-feedback.html
│   │           ├── email-reminder.html
│   │           └── email-completed.html
│   └── test/
│       └── java/com/ovi/hospitalfeedback/
│           └── (unit tests)
├── pom.xml                             # Maven dependencies
├── Dockerfile
└── README.md
```

---

## 🔧 Dependencies chính

### Frontend (package.json)

```json
{
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.0",
    "axios": "^1.6.0",
    "element-plus": "^2.5.0",
    "chart.js": "^4.4.0",
    "vue-chartjs": "^5.3.0",
    "@element-plus/icons-vue": "^2.3.0",
    "dayjs": "^1.11.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0"
  }
}
```

### Backend (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
        <version>0.9.1</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>

    <!-- ModelMapper -->
    <dependency>
        <groupId>org.modelmapper</groupId>
        <artifactId>modelmapper</artifactId>
        <version>3.2.0</version>
    </dependency>
</dependencies>
```

---

## 🔐 Security & Authentication

### JWT Authentication Flow:

```
1. User login (username/password)
   ↓
2. Spring Security verify credentials
   ↓
3. Generate JWT token (valid 24h)
   ↓
4. Return token to Vue.js
   ↓
5. Vue.js lưu token vào localStorage
   ↓
6. Mọi request sau đó: Header Authorization: Bearer {token}
   ↓
7. Spring Boot verify token → Allow/Deny
```

### Roles & Permissions:

```java
// Spring Security Roles
ROLE_ADMIN       // Full access
ROLE_LEADER      // View reports only
ROLE_RECEIVER    // Create feedbacks
ROLE_HANDLER     // Process feedbacks
ROLE_VIEWER      // View feedbacks of department
```

---

## 📡 REST API Endpoints

### Authentication:
- `POST /api/auth/login` - Login
- `POST /api/auth/logout` - Logout
- `GET /api/auth/me` - Get current user info

### Feedbacks:
- `GET /api/feedbacks` - List all (with filters)
- `GET /api/feedbacks/{id}` - Get detail
- `POST /api/feedbacks` - Create new
- `PUT /api/feedbacks/{id}` - Update
- `DELETE /api/feedbacks/{id}` - Delete
- `GET /api/feedbacks/my` - Get my assigned feedbacks
- `PUT /api/feedbacks/{id}/process` - Update process status

### Departments:
- `GET /api/departments` - List
- `POST /api/departments` - Create
- `PUT /api/departments/{id}` - Update
- `DELETE /api/departments/{id}` - Delete

### Doctors:
- `GET /api/doctors` - List
- `GET /api/doctors?departmentId={id}` - List by department
- `POST /api/doctors` - Create
- `PUT /api/doctors/{id}` - Update
- `DELETE /api/doctors/{id}` - Delete

### Users:
- `GET /api/users` - List (Admin only)
- `POST /api/users` - Create
- `PUT /api/users/{id}` - Update
- `DELETE /api/users/{id}` - Delete

### File Upload:
- `POST /api/upload/feedback-images` - Upload ảnh phản ánh
- `POST /api/upload/process-images` - Upload ảnh xử lý
- `GET /api/upload/images/{filename}` - Get image
- `DELETE /api/upload/images/{id}` - Delete image

### Reports:
- `GET /api/reports/dashboard` - Dashboard summary
- `GET /api/reports/by-department` - Report by department
- `GET /api/reports/by-doctor` - Report by doctor
- `GET /api/reports/export-excel` - Export Excel
- `GET /api/reports/export-pdf` - Export PDF

---

## 💾 Database Schema

### Table: users
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL,
    department_id BIGINT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    FOREIGN KEY (department_id) REFERENCES departments(id)
);
```

### Table: departments
```sql
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    manager_id BIGINT,
    default_handler_id BIGINT,
    notification_email VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manager_id) REFERENCES users(id),
    FOREIGN KEY (default_handler_id) REFERENCES users(id)
);
```

### Table: doctors
```sql
CREATE TABLE doctors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    specialty VARCHAR(100),
    department_id BIGINT NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);
```

### Table: feedbacks
```sql
CREATE TABLE feedbacks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) UNIQUE NOT NULL,
    received_date DATETIME NOT NULL,
    channel VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    department_id BIGINT NOT NULL,
    doctor_id BIGINT,
    level VARCHAR(20) NOT NULL,
    status VARCHAR(20) DEFAULT 'NEW',
    handler_id BIGINT,
    receiver_id BIGINT NOT NULL,
    process_note TEXT,
    completed_date DATETIME,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    FOREIGN KEY (handler_id) REFERENCES users(id),
    FOREIGN KEY (receiver_id) REFERENCES users(id)
);
```

### Table: feedback_images
```sql
CREATE TABLE feedback_images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feedback_id BIGINT NOT NULL,
    image_path VARCHAR(255) NOT NULL,
    image_type VARCHAR(20) NOT NULL,
    uploaded_by BIGINT NOT NULL,
    uploaded_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (feedback_id) REFERENCES feedbacks(id),
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
);
```

### Table: feedback_logs
```sql
CREATE TABLE feedback_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    feedback_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    action VARCHAR(50) NOT NULL,
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    note TEXT,
    created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (feedback_id) REFERENCES feedbacks(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 🎨 UI/UX Guidelines

### Design System:
- **Colors**: Element Plus default theme (Blue primary)
- **Typography**: Inter hoặc Roboto
- **Icons**: Element Plus icons + Font Awesome
- **Layout**: Sidebar navigation (desktop), Bottom nav (mobile)

### Responsive Breakpoints:
- Mobile: < 768px
- Tablet: 768px - 1024px
- Desktop: > 1024px

### Components sử dụng (Element Plus):
- **el-table** - Tables
- **el-form** - Forms
- **el-select** - Dropdowns
- **el-upload** - File upload
- **el-image** - Image preview
- **el-dialog** - Modals
- **el-notification** - Toast notifications
- **el-badge** - Status badges
- **el-card** - Cards

---

## 📧 Email Configuration

### Gmail SMTP Setup:

```properties
# application.properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Lưu ý:** Phải bật 2FA và tạo App Password trong Gmail

---

## 🚀 Deployment

### Development:
```bash
# Frontend
cd frontend
npm run dev         # http://localhost:5173

# Backend
cd backend
mvn spring-boot:run # http://localhost:8080
```

### Production (VPS):

```bash
# Build Frontend
cd frontend
npm run build       # → dist/

# Deploy Frontend to Nginx
sudo cp -r dist/* /var/www/html/

# Build Backend
cd backend
mvn clean package   # → target/app.jar

# Run Backend (with systemd)
java -jar target/hospital-feedback-0.0.1.jar
```

---

## 💰 Chi phí Chi tiết (Cập nhật)

| Hạng mục | Chi phí | Ghi chú |
|---|---|---|
| **Frontend Dev (Vue.js)** | 7-8 triệu | 1 dev x 1.5 tháng |
| **Backend Dev (Spring Boot)** | 8-10 triệu | 1 dev x 2 tháng |
| **BA/Design** | 3-4 triệu | BA + UI/UX |
| **VPS Hosting (1 năm)** | 2.4 triệu | $10/month x 12 |
| **Domain (.vn)** | 500k | /năm |
| **SSL Certificate** | Free | Let's Encrypt |
| **Testing/QA** | 2 triệu | 2 tuần |
| **Training** | 1 triệu | User training |
| **Dự phòng** | 3-4 triệu | Adjustments |
| **TỔNG** | **27-32 triệu** | |

---

---

**Lead BA:** Nguyễn Thanh Tuấn
**Lead BE:** Nguyễn Thanh Tuấn
**Ngày phê duyệt:** 27/11/2025
**Next step:** BA tạo Database Schema chi tiết & API Specs

