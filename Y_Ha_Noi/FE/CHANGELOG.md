# Changelog

Tất cả các thay đổi đáng chú ý trong dự án này sẽ được ghi lại trong file này.

Format dựa trên [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
và dự án này tuân thủ [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.4.1] - 2025-12-08

### Fixed
- **Report Pages**:
  - Fix trang `reports/with-images` không hiển thị dữ liệu (sửa tên tham số API: `startDate`/`endDate` → `dateFrom`/`dateTo`)
  - Fix đếm số tổng phản ánh ở `ReportByDoctor` detail dialog không chính xác (dùng `detailStats` computed từ `doctorFeedbacks` thay vì `selectedDoctor.total`)
  - Cải thiện accuracy của summary cards trong doctor detail dialog

### Changed
- **ReportByDoctor**:
  - Thêm computed property `detailStats` để tính lại số liệu từ `doctorFeedbacks` thay vì dùng `selectedDoctor`
  - Đảm bảo số đếm trong summary cards khớp với danh sách feedbacks hiển thị

## [1.4.0] - 2025-12-08

### Fixed
- **Query Fixes**:
  - Fix lỗi SQL syntax error khi query feedbacks với optional `doctorId` parameter
  - Fix lỗi trang `reports/by-department` và `reports/by-doctor` không load được dữ liệu
  - Fix lỗi API `/api/feedbacks` trả về 400 Bad Request với native SQL queries
  - Cải thiện xử lý optional parameters trong feedback search queries

### Changed
- **Report Pages**:
  - Cải thiện error handling cho report pages khi API fails
  - Đảm bảo tất cả filter parameters được xử lý đúng cách

## [1.3.1] - 2025-12-07

### Added
- **Feedback Processing History**:
  - Tách biệt "Nội dung xử lý" và "Ghi chú" trong UI và hiển thị riêng trong timeline
  - Hiển thị file đính kèm trong timeline lịch sử xử lý với link download
  - Upload images khi xử lý feedback và lưu vào history
  - Hiển thị attachments với icon Paperclip và link để mở file

- **UI Improvements**:
  - Cải thiện timeline để hiển thị riêng "Nội dung xử lý" (content) và "Ghi chú" (note)
  - Thêm styling riêng cho timeline-note với màu text-secondary
  - Cải thiện visual hierarchy trong timeline items

### Changed
- **Processing Form**:
  - Cải thiện logic upload: upload images trước, lấy imageIds, rồi gửi trong request
  - Map cả `content` và `note` từ API response riêng biệt
  - Cải thiện error handling cho upload process

- **Timeline Display**:
  - Cập nhật `MyFeedbacks.vue` để hiển thị cả `content` và `note` riêng biệt
  - Cập nhật `FeedbackRatings.vue` để ưu tiên `content` trước `note`
  - Cập nhật `FeedbackTimeline.vue` để hiển thị images với link download

- **Data Mapping**:
  - Cải thiện mapping từ `FeedbackHistoryDTO` để tách `content` và `note`
  - Map `images` từ API response thành `attachments` với đầy đủ thông tin (id, name, url)

### Fixed
- **Bug Fixes**:
  - Fix lỗi "Ghi chú" và "File đính kèm" không được lưu vào history
  - Fix lỗi timeline không hiển thị attachments
  - Fix lỗi UI chỉ có 1 trường `note` trong khi có 2 trường riêng biệt
  - Fix lỗi upload images không được gửi kèm trong process request

## [1.3.1] - 2025-12-07

### Added
- **Feedback Processing History**:
  - Tách biệt "Nội dung xử lý" và "Ghi chú" trong UI và hiển thị riêng trong timeline
  - Hiển thị file đính kèm trong timeline lịch sử xử lý với link download
  - Upload images khi xử lý feedback và lưu vào history
  - Hiển thị attachments với icon Paperclip và link để mở file

- **UI Improvements**:
  - Cải thiện timeline để hiển thị riêng "Nội dung xử lý" (content) và "Ghi chú" (note)
  - Thêm styling riêng cho timeline-note với màu text-secondary
  - Cải thiện visual hierarchy trong timeline items

### Changed
- **Processing Form**:
  - Cải thiện logic upload: upload images trước, lấy imageIds, rồi gửi trong request
  - Map cả `content` và `note` từ API response riêng biệt
  - Cải thiện error handling cho upload process

- **Timeline Display**:
  - Cập nhật `MyFeedbacks.vue` để hiển thị cả `content` và `note` riêng biệt
  - Cập nhật `FeedbackRatings.vue` để ưu tiên `content` trước `note`
  - Cập nhật `FeedbackTimeline.vue` để hiển thị images với link download

- **Data Mapping**:
  - Cải thiện mapping từ `FeedbackHistoryDTO` để tách `content` và `note`
  - Map `images` từ API response thành `attachments` với đầy đủ thông tin (id, name, url)

### Fixed
- **Bug Fixes**:
  - Fix lỗi "Ghi chú" và "File đính kèm" không được lưu vào history
  - Fix lỗi timeline không hiển thị attachments
  - Fix lỗi UI chỉ có 1 trường `note` trong khi có 2 trường riêng biệt
  - Fix lỗi upload images không được gửi kèm trong process request

## [1.3.0] - 2025-12-07

### Added
- **Notification System Improvements**:
  - Thêm hỗ trợ notification type `RATING` cho đánh giá mới
  - Click notification mở popup thay vì navigate trực tiếp
  - Notification về feedback mở popup feedback detail
  - Notification về rating mở popup đánh giá trực tiếp
  - Hỗ trợ tìm feedback bằng code từ notification message

- **Feedback Detail Popup**:
  - Thêm popup feedback detail cho Dashboard và Feedback List
  - Click vào row trong table mở popup thay vì navigate
  - Popup responsive và nằm trong màn hình

- **Rating System**:
  - Thêm lịch sử xử lý vào popup đánh giá
  - Hiển thị đánh giá trung bình và trạng thái đánh giá của user
  - Tự động cập nhật menu badge count sau khi đánh giá

- **Handler Assignment**:
  - Tự động phân công handler theo phòng ban khi tạo feedback
  - Thêm UI phân công handler trên trang Feedback List
  - Filter "Thao tác" để lọc feedback đã/chưa phân công

- **Performance Optimizations**:
  - Sử dụng `shallowRef` cho arrays lớn
  - Virtual scrolling cho tables
  - Request caching và retry logic
  - Code splitting và bundle optimization
  - Debounced chart re-renders

- **Testing**:
  - Thêm Playwright tests cho processing section
  - Thêm unit tests với Vitest
  - Cải thiện test reliability với better selectors và waits

### Changed
- **UI/UX Improvements**:
  - Cải thiện responsive design cho mobile
  - Thêm ARIA attributes cho accessibility
  - Cải thiện error messages và loading states
  - Tối ưu column widths để tránh truncation

- **Data Display**:
  - Thêm cột "Bác sĩ" vào các bảng feedback
  - Hiển thị đầy đủ thông tin: Phòng ban, Bác sĩ, Người xử lý
  - Sắp xếp feedback theo Ngày nhận và Số PA (descending)

- **Notification Handling**:
  - Cải thiện logic redirect cho các loại notification khác nhau
  - Fallback tìm feedback bằng code nếu không có feedbackId

### Fixed
- **Bug Fixes**:
  - Fix lỗi console khi mở dialog rating và my-feedbacks
  - Fix lỗi `processHistory.length` undefined
  - Fix lỗi `processForm.attachments` undefined
  - Fix lỗi database constraint cho notification type RATING
  - Fix lỗi duplicate key khi tạo feedback code
  - Fix lỗi menu badge count không cập nhật sau actions
  - Fix lỗi notification không hiển thị cho handler
  - Fix lỗi click notification không tìm thấy feedback

- **Data Issues**:
  - Fix Dashboard charts không hiển thị dữ liệu
  - Fix thiếu dữ liệu Phòng ban, Bác sĩ, Người xử lý
  - Fix sorting feedback không đúng thứ tự
  - Fix thiếu "Người tiếp nhận" và "Kênh tiếp nhận" trong feedback detail

## [1.2.0] - 2025-12-06

### Added
- Thêm tính năng đánh giá bác sĩ sau khi xử lý hoàn thành
- Thêm trang "Của tôi" cho handler xem feedback được phân công
- Thêm xử lý feedback với form và lịch sử xử lý
- Thêm notification system với real-time updates

### Changed
- Cải thiện UI/UX cho các trang feedback
- Tối ưu performance với shallowRef và computed properties

## [1.1.0] - 2025-12-05

### Added
- Thêm Dashboard với charts và stats
- Thêm filter và search cho feedback list
- Thêm quản lý departments và doctors

## [1.0.0] - 2025-12-04

### Added
- Release ban đầu
- Hệ thống quản lý phản ánh cơ bản
- Authentication và authorization
- CRUD operations cho feedback
- Admin panel cho quản lý users, departments, doctors

[1.4.1]: https://github.com/your-repo/compare/v1.4.0...v1.4.1
[1.4.0]: https://github.com/your-repo/compare/v1.3.1...v1.4.0
[1.3.1]: https://github.com/your-repo/compare/v1.3.0...v1.3.1
[1.3.0]: https://github.com/your-repo/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/your-repo/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/your-repo/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/your-repo/releases/tag/v1.0.0

