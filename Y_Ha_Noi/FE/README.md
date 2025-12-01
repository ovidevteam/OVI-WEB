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

Tạo file `.env` với các biến sau:

```env
VITE_APP_TITLE=Quản lý Phản ánh - BV Y Hà Nội
VITE_API_BASE_URL=http://localhost:8080/api
VITE_UPLOAD_MAX_SIZE=5242880
VITE_UPLOAD_MAX_FILES=10
```

## API Backend

Backend sử dụng Spring Boot 3, chạy trên port 8080. Xem chi tiết tại folder `BE/`.

## Liên hệ

- **Lead BA/BE:** Nguyễn Thanh Tuấn
- **Client:** Bệnh viện Y Hà Nội

---

**Created:** 2025-11-27
**Version:** 1.0.0

