# Yêu cầu Nghiệp vụ - Hệ thống Quản lý Phản ánh

## 1. Phân quyền User

### Vai trò hệ thống:
- **Admin**: Quản trị hệ thống, phân quyền, quản lý danh mục
- **Lãnh đạo**: Xem báo cáo tổng hợp, theo dõi KPI
- **Người tiếp nhận**: Nhập phản ánh từ các kênh
- **Người xử lý**: Xử lý phản ánh được phân công
- **Người theo dõi**: Xem phản ánh của phòng/khoa

### Quyền hạn:
- Admin: Full access (CRUD tất cả)
- Lãnh đạo: Read all, Export reports
- Người tiếp nhận: Create phản ánh, View own
- Người xử lý: Update phản ánh được assign, Upload hình xử lý
- Người theo dõi: View phản ánh của phòng mình

---

## 2. Nhập Phòng ban, Bác sĩ

### Danh mục Phòng ban:
- Mã phòng (auto)
- Tên phòng
- Trưởng phòng
- Người xử lý mặc định
- Email nhận thông báo
- Trạng thái: Hoạt động/Ngừng

### Danh mục Bác sĩ:
- Mã BS (auto)
- Họ tên
- Chuyên khoa
- Phòng ban
- Email
- Số điện thoại
- Trạng thái: Hoạt động/Ngừng

---

## 3. Ghi nhận Phản ánh

### Thông tin phản ánh:
- **Số phản ánh** (auto: PA-YYYYMMDD-XXX)
- **Ngày tiếp nhận** (auto)
- **Kênh tiếp nhận**: Hotline, Email, Trực tiếp, Zalo, Facebook, Khác
- **Nội dung phản ánh** (textarea)
- **Phòng liên quan** (dropdown)
- **Bác sĩ liên quan** (dropdown - optional)
- **Mức độ**: Khẩn cấp, Cao, Trung bình, Thấp
- **Upload hình ảnh**: Tối đa 10 ảnh (jpg, png, < 5MB/ảnh)
- **Người tiếp nhận** (auto: current user)

### Tự động:
- Phân công người xử lý theo phòng ban (lấy từ danh mục)
- Gửi email thông báo cho người xử lý
- Trạng thái mặc định: "Chưa xử lý"

---

## 4. Phân công Xử lý

### Phân công tự động:
- Khi nhập phản ánh → Hệ thống tự động assign người xử lý theo phòng ban

### Phân công thủ công:
- Admin/Lãnh đạo có thể đổi người xử lý
- Gửi email thông báo cho người xử lý mới

### Theo dõi:
- Hiển thị danh sách phản ánh được assign
- Sắp xếp theo: Mức độ, Ngày nhận, Quá hạn

---

## 5. Thông báo Email

### Email gửi khi nào:
- **Email 1**: Khi có phản ánh mới → Gửi cho người xử lý
- **Email 2**: Nhắc nhở khi quá 3 ngày chưa xử lý → Gửi cho người xử lý + Trưởng phòng
- **Email 3**: Khi hoàn thành → Gửi cho người tiếp nhận + Lãnh đạo

### Nội dung email:
```
Subject: [Phản ánh mới] PA-20251127-001 - Phòng Nội

Kính gửi Bác sĩ [Tên],

Có phản ánh mới cần xử lý:
- Số PA: PA-20251127-001
- Nội dung: [Nội dung tóm tắt]
- Phòng: Phòng Nội
- Mức độ: Cao
- Hạn xử lý: 3 ngày

Vui lòng vào hệ thống để xử lý:
http://phananhbenhnien.vn/login

Trân trọng,
Hệ thống Quản lý Phản ánh
```

---

## 6. Xử lý Phản ánh

### Quy trình:
1. Người xử lý login hệ thống
2. Vào "Phản ánh của tôi"
3. Click vào phản ánh cần xử lý
4. Cập nhật:
   - **Trạng thái**: Chưa xử lý → Đang xử lý → Hoàn thành
   - **Ghi chú xử lý** (textarea)
   - **Upload hình ảnh minh chứng** (trước/sau xử lý - tối đa 10 ảnh)
   - **Ngày hoàn thành** (auto khi chọn "Hoàn thành")
5. Lưu → Gửi email thông báo hoàn thành

---

## 7. Báo cáo Thống kê

### Báo cáo cho Lãnh đạo:

#### **Dashboard tổng quan:**
- Tổng số phản ánh (tháng này, năm này)
- Phản ánh đang xử lý
- Phản ánh quá hạn
- Thời gian xử lý trung bình
- Biểu đồ: Phản ánh theo tháng (Line chart)
- Biểu đồ: Phản ánh theo phòng (Bar chart)

#### **Báo cáo chi tiết:**
- **Theo phòng ban**: Số lượng, tỷ lệ hoàn thành, thời gian xử lý TB
- **Theo bác sĩ**: Top 10 bác sĩ có nhiều phản ánh nhất
- **Theo mức độ**: Khẩn cấp, Cao, Trung bình, Thấp
- **Theo kênh tiếp nhận**: Hotline, Email, Trực tiếp...
- **Theo trạng thái**: Chưa xử lý, Đang xử lý, Hoàn thành
- **Hiển thị hình ảnh**: Gallery hình ảnh trong báo cáo

#### **Export:**
- Export Excel (danh sách phản ánh)
- Export PDF (báo cáo tổng hợp kèm hình ảnh)
- Lọc theo: Ngày, Phòng, Bác sĩ, Trạng thái

---

## 8. Quản lý Hình ảnh

### Upload hình:
- **Vị trí 1**: Khi nhập phản ánh (hình minh chứng vấn đề)
- **Vị trí 2**: Khi xử lý (hình trước/sau xử lý)
- **Giới hạn**: Tối đa 10 ảnh/phản ánh, < 5MB/ảnh
- **Format**: JPG, PNG, WEBP

### Hiển thị:
- Gallery ảnh trong chi tiết phản ánh
- Lightbox khi click vào ảnh (xem phóng to)
- Thumbnail trong danh sách phản ánh
- Ảnh đại diện trong báo cáo

### Lưu trữ:
- Lưu trên server (folder `uploads/`)
- Đặt tên: `PA-{số_phản_ánh}_{timestamp}_{index}.jpg`
- Tự động resize để tiết kiệm dung lượng

---

## 🔧 Yêu cầu kỹ thuật

### Bảo mật:
- Login bắt buộc (username/password)
- Session timeout: 30 phút
- Password phải mạnh (> 8 ký tự)
- Chỉ xem/sửa phản ánh theo quyền

### Hiệu năng:
- Load trang < 2 giây
- Hỗ trợ 50 users đồng thời
- Backup database hàng ngày

### Responsive:
- Responsive trên mobile, tablet, desktop
- Ưu tiên desktop (vì nhập liệu nhiều)

---

## 💻 Yêu cầu Hosting & Domain

### Scenario 1: Bệnh viện ĐÃ CÓ Server & Domain (Giảm chi phí)

**Yêu cầu server tối thiểu:**
- **OS:** Ubuntu 20.04+ hoặc CentOS 7+
- **RAM:** Tối thiểu 2GB (khuyến nghị 4GB)
- **CPU:** 2 cores
- **Storage:** 20GB (cho database + hình ảnh)
- **Network:** Public IP, port 80/443 mở

**Phần mềm cần cài:**
- Java 17+ (cho Spring Boot)
- MySQL 8 (hoặc PostgreSQL)
- Nginx (web server/reverse proxy)
- Node.js 18+ (build frontend)

**Domain đã có:**
- Ví dụ: `benhnienyhano i.vn` hoặc subdomain: `phananhbenhnien.vn`
- Cần quyền truy cập DNS để cấu hình

**Chi phí giảm:**
- ❌ Không mất phí VPS (2.4 triệu/năm)
- ❌ Không mất phí Domain (500k/năm)
- ✅ **Tổng ngân sách giảm: 24-27 triệu** (thay vì 27-32 triệu)

**Chi phí còn lại:**
- Development: 15-18 triệu
- BA/Design: 3-4 triệu
- Testing: 2 triệu
- Training: 1 triệu
- Dự phòng: 3-4 triệu

---

### Scenario 2: Bệnh viện CHƯA CÓ Server & Domain (Chi phí đầy đủ)

**Cần mua:**

#### **1. VPS Hosting (Khuyến nghị)**

| Provider | Cấu hình | Giá/tháng | Giá/năm | Ghi chú |
|---|---|---|---|---|
| **DigitalOcean** | 2GB RAM, 2 CPU, 50GB SSD | $12 (~280k) | $144 (~3.4tr) | Stable, dễ dùng |
| **Vultr** | 2GB RAM, 1 CPU, 55GB SSD | $12 (~280k) | $144 (~3.4tr) | Có DC tại Singapore |
| **AWS Lightsail** | 2GB RAM, 1 CPU, 60GB SSD | $10 (~240k) | $120 (~2.9tr) | Dễ scale |
| **VPS Việt Nam** | 2GB RAM, 2 CPU, 40GB SSD | 200-250k | 2.4-3tr | Hỗ trợ tiếng Việt |

**Khuyến nghị:** DigitalOcean hoặc Vultr (Stable, tài liệu nhiều)

#### **2. Domain Name**

| Loại | Giá/năm | Ghi chú |
|---|---|---|
| **.vn** | 400-600k | Domain Việt Nam |
| **.com.vn** | 350-500k | Domain doanh nghiệp VN |
| **.com** | 300-400k | Domain quốc tế |

**Khuyến nghị:** `.vn` hoặc `.com.vn` (uy tín hơn cho bệnh viện)

#### **3. SSL Certificate**

- **Free:** Let's Encrypt (khuyến nghị - tự động renew)
- **Paid:** 500k - 1tr/năm (nếu cần Extended Validation)

**Chi phí tổng (năm đầu):**
- VPS: 2.9-3.4 triệu
- Domain: 400-600k
- SSL: Free (Let's Encrypt)
- **Tổng chi phí hosting: 3.3-4 triệu/năm**

**Chi phí development + hosting:**
- Development: 15-18 triệu
- BA/Design: 3-4 triệu
- Hosting năm 1: 3.3-4 triệu
- Testing: 2 triệu
- Training: 1 triệu
- Dự phòng: 3-4 triệu
- **TỔNG: 27.3-35 triệu**

---

### Scenario 3: Hosting Rẻ nhất (Shared Hosting)

**Nếu traffic thấp (< 1000 visits/day):**

| Provider | Cấu hình | Giá/năm | Ghi chú |
|---|---|---|---|
| **Hostinger** | 2GB RAM, MySQL, cPanel | 1.2-1.5tr | Rẻ nhưng giới hạn |
| **PA Việt Nam** | 2GB RAM, MySQL | 800k-1.2tr | Hỗ trợ tiếng Việt |

**Hạn chế:**
- ❌ Không cài được Java/Spring Boot trực tiếp (chỉ support PHP/Node.js)
- ❌ Phải chuyển sang PHP + MySQL hoặc Node.js
- ✅ Rẻ hơn VPS (tiết kiệm 2 triệu/năm)

**Nếu dùng Shared Hosting → Phải đổi tech stack:**
- Frontend: Vue.js (OK)
- Backend: ❌ Spring Boot → ✅ **PHP Laravel** hoặc **Node.js Express**

---

## 📊 So sánh Chi phí

| Scenario | Development | Hosting/năm | Domain/năm | TỔNG (năm 1) |
|---|---|---|---|---|
| **Có sẵn Server & Domain** | 24-27tr | 0đ | 0đ | **24-27tr** |
| **VPS + Domain mới** | 24-27tr | 2.9-3.4tr | 400-600k | **27.3-31tr** |
| **Shared Hosting** | 24-27tr | 1.2-1.5tr | 400-600k | **25.6-29.1tr** |

---

## ✅ Khuyến nghị

### **Nếu bệnh viện ĐÃ CÓ server:**
- ✅ Dùng luôn, tiết kiệm chi phí
- ✅ Cần kiểm tra cấu hình server có đủ yêu cầu không
- ✅ Cần quyền truy cập SSH để deploy

### **Nếu bệnh viện CHƯA CÓ server:**
- ✅ **Khuyến nghị:** Thuê VPS (DigitalOcean/Vultr)
- ✅ Lý do: Linh hoạt, dễ scale, full control
- ✅ Tech stack: Vue.js + Spring Boot (như đã thiết kế)

### **Nếu muốn tiết kiệm tối đa:**
- ⚠️ Dùng Shared Hosting
- ⚠️ Phải đổi tech stack: Vue.js + **PHP Laravel** (thay Spring Boot)
- ⚠️ Giảm được ~2 triệu/năm nhưng ít linh hoạt hơn

---

## 📦 Modules cần phát triển

1. ✅ **Module Phân quyền** (Users & Roles)
2. ✅ **Module Danh mục** (Phòng ban, Bác sĩ)
3. ✅ **Module Phản ánh** (Nhập, Xử lý, Theo dõi)
4. ✅ **Module Thông báo** (Email notification)
5. ✅ **Module Báo cáo** (Dashboard, Reports)
6. ✅ **Module Upload** (Quản lý hình ảnh)

---

**Lead BA:** Nguyễn Thanh Tuấn
**Lead BE:** Nguyễn Thanh Tuấn
**Ngày phê duyệt:** 27/11/2025
**Next step:** BA viết Functional Specs chi tiết

