# Smoke Test Report - API vs Mock Data

## Tổng quan
Kiểm tra tất cả các trang để đảm bảo:
- **DEMO_MODE=true**: Sử dụng mock data
- **DEMO_MODE=false**: Gọi API thật từ backend

## Kết quả kiểm tra

### ✅ Đã sửa xong

1. **Login.vue**
   - ✅ Demo Accounts chỉ hiển thị khi DEMO_MODE=true
   - ✅ Forgot password gọi API thật

2. **Dashboard.vue**
   - ✅ Gọi API thật: `reportService.getDashboard()`, `reportService.getMonthlyStats()`, `reportService.getByDepartment()`, `feedbackService.getList()`

3. **FeedbackRatings.vue**
   - ✅ DEMO_MODE=true: Dùng mockFeedbackList
   - ✅ DEMO_MODE=false: Gọi `ratingService.getCompletedFeedbacks()`, `ratingService.submitRating()`

4. **authService.js**
   - ✅ Xóa fallback tự động
   - ✅ DEMO_MODE=true: Dùng demo login
   - ✅ DEMO_MODE=false: Gọi API thật

5. **ratingService.js**
   - ✅ Đọc DEMO_MODE từ env variable

### ✅ Đã sửa xong

6. **FeedbackList.vue**
   - ✅ Chỉ fallback demo data khi DEMO_MODE=true
   - ✅ DEMO_MODE=false: Hiển thị lỗi thay vì fallback

7. **FeedbackDetail.vue**
   - ✅ Chỉ fallback demo data khi DEMO_MODE=true
   - ✅ DEMO_MODE=false: Hiển thị lỗi thay vì fallback

8. **MyFeedbacks.vue**
   - ✅ Chỉ fallback demo data khi DEMO_MODE=true
   - ✅ DEMO_MODE=false: Hiển thị lỗi thay vì fallback

9. **UserManagement.vue**
   - ✅ Chỉ fallback demo data khi DEMO_MODE=true
   - ✅ DEMO_MODE=false: Hiển thị lỗi thay vì fallback

10. **ReportByDepartment.vue**
    - ✅ Chỉ fallback demo data khi DEMO_MODE=true
    - ✅ DEMO_MODE=false: Hiển thị lỗi thay vì fallback

11. **ReportByDoctor.vue**
    - ✅ Chỉ fallback demo data khi DEMO_MODE=true
    - ✅ DEMO_MODE=false: Hiển thị lỗi thay vì fallback

12. **ReportWithImages.vue**
    - ✅ Chỉ fallback demo data khi DEMO_MODE=true
    - ✅ DEMO_MODE=false: Hiển thị lỗi thay vì fallback

## Pattern cần áp dụng

```javascript
const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

const fetchData = async () => {
  try {
    if (DEMO_MODE) {
      // Use mock data
      data.value = mockData
    } else {
      // Call real API
      const response = await service.getData()
      data.value = response.data
    }
  } catch (error) {
    if (DEMO_MODE) {
      // Fallback to mock in demo mode
      data.value = mockData
    } else {
      // Show error, don't fallback
      ElMessage.error('Lỗi khi tải dữ liệu')
      console.error(error)
    }
  }
}
```

## Components

### ✅ Đã sửa xong

13. **Sidebar.vue**
    - ✅ Badge counts chỉ dùng mock khi DEMO_MODE=true
    - ✅ DEMO_MODE=false: Gọi API để lấy feedback stats thật
    - ✅ Auto refresh mỗi 30 giây

14. **Header.vue**
    - ✅ Notifications chỉ dùng mock khi DEMO_MODE=true
    - ✅ DEMO_MODE=false: Gọi API để lấy notifications thật
    - ✅ Auto refresh mỗi 30 giây

### ✅ Đã sửa xong

15. **DepartmentManagement.vue**
    - ✅ Chỉ fallback demo data khi DEMO_MODE=true
    - ✅ DEMO_MODE=false: Hiển thị lỗi thay vì fallback

16. **DoctorManagement.vue**
    - ✅ Chỉ fallback demo data khi DEMO_MODE=true
    - ✅ DEMO_MODE=false: Hiển thị lỗi thay vì fallback

17. **FeedbackCreate.vue**
    - ✅ Departments và doctors chỉ fallback demo khi DEMO_MODE=true
    - ✅ DEMO_MODE=false: Hiển thị lỗi thay vì fallback

## Tổng kết

- **Đã sửa**: 17 files ✅
- **Cần sửa**: 0 files
- **Tổng số**: 12 views + 2 components + 3 admin/forms

## Kết quả

Tất cả các trang đã được sửa để:
- **DEMO_MODE=true**: Sử dụng mock data để test không cần backend
- **DEMO_MODE=false**: Gọi API thật từ backend, không tự động fallback sang demo

## Pattern đã áp dụng

Tất cả các trang đều sử dụng pattern:
```javascript
const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

try {
  // Call API
  const response = await service.getData()
  data.value = response.data
} catch (error) {
  if (DEMO_MODE) {
    // Fallback to mock data only in demo mode
    data.value = mockData
  } else {
    // Show error, don't fallback
    ElMessage.error('Lỗi khi tải dữ liệu')
    console.error(error)
  }
}
```

