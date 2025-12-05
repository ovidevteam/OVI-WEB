# Mock Data - Demo Mode

File này chứa tất cả mock data được sử dụng khi `DEMO_MODE=true`.

## Cấu trúc

Tất cả mock data được định nghĩa trong `db.js` và được export để sử dụng trong các component.

## Cách sử dụng

### 1. Import mock data trong component:

```javascript
import { mockUsers, mockDepartments, mockFeedbacks } from '@/mock/db'
```

### 2. Sử dụng khi DEMO_MODE=true:

```javascript
const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

if (DEMO_MODE) {
  // Sử dụng mock data từ db.js
  users.value = [...mockUsers]
  return
}

// Gọi API thật khi DEMO_MODE=false
try {
  const response = await userService.getList()
  users.value = response.data
} catch (error) {
  // Xử lý lỗi
}
```

## Danh sách Mock Data

- `mockUsers` - Danh sách users
- `mockDepartments` - Danh sách phòng ban đầy đủ
- `mockDepartmentsSimple` - Danh sách phòng ban đơn giản (chỉ id và name)
- `mockDoctors` - Danh sách bác sĩ
- `mockFeedbacks` - Danh sách phản ánh
- `mockDashboardStats` - Thống kê dashboard
- `mockMonthlyStats` - Thống kê theo tháng
- `mockDepartmentStats` - Thống kê theo phòng ban
- `mockReportByDepartment` - Báo cáo theo phòng ban
- `mockReportByDoctor` - Báo cáo theo bác sĩ
- `mockReportWithImages` - Báo cáo có hình ảnh
- `mockNotifications` - Thông báo
- `mockFeedbackStats` - Thống kê badge counts
- `mockDoctorFeedbacks` - Phản ánh theo bác sĩ
- `mockDepartmentFeedbacks` - Phản ánh theo phòng ban
- `mockProcessHistory` - Lịch sử xử lý
- `mockRatings` - Đánh giá

## Lưu ý

- Tất cả mock data nên được import từ `@/mock/db`
- Không hardcode mock data trực tiếp trong component
- Khi thêm mock data mới, cập nhật file này và export trong `db.js`

