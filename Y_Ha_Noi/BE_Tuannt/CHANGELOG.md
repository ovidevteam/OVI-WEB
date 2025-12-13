# Changelog

Tất cả các thay đổi đáng chú ý trong dự án này sẽ được ghi lại trong file này.

Format dựa trên [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
và dự án này tuân thủ [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.4.2] - 2025-12-08

### Added
- **Report With Images**:
  - Implement method `getFeedbacksWithImages` trong `ReportService` để query feedbacks có images
  - Filter feedbacks theo `dateFrom`, `dateTo`, và `departmentId`
  - Load và map images cho mỗi feedback trong response
  - Sort feedbacks theo `code DESC`

### Fixed
- **Report Pages**:
  - Fix trang `reports/with-images` không hiển thị dữ liệu (method trả về empty list)
  - Fix frontend gửi sai tên tham số (`startDate`/`endDate` → `dateFrom`/`dateTo`)

## [1.4.1] - 2025-12-08

### Fixed
- **Query Fixes**:
  - Fix lỗi SQL syntax error với PostgreSQL type casting (`::date` → `CAST(... AS DATE)`)
  - Tách query thành 2 method riêng: `search` (không có doctorId) và `searchWithDoctor` (có doctorId)
  - Fix lỗi xử lý NULL parameters trong native SQL queries
  - Fix lỗi `keywordPattern` handling trong LIKE queries
  - Cải thiện xử lý optional parameters (`doctorId`, `keywordPattern`) trong native queries

## [1.4.0] - 2025-12-07

### Added
- **Feedback Processing History**:
  - Thêm field `content` (Nội dung xử lý) vào FeedbackHistory entity
  - Thêm field `imageIds` (JSON array) vào FeedbackHistory để lưu danh sách file đính kèm
  - Tách biệt "Nội dung xử lý" và "Ghi chú" trong UI và database
  - Hiển thị file đính kèm trong timeline lịch sử xử lý
  - Upload và lưu images khi xử lý feedback với endpoint `/upload/process-images`

- **API Improvements**:
  - Thêm `content` và `images` vào FeedbackHistoryDTO
  - Cải thiện `getProcessHistory` để load và trả về chi tiết images
  - Cải thiện `processFeedback` để xử lý cả `content` và `note` riêng biệt

### Changed
- **Database Schema**:
  - Thêm cột `content` (TEXT) vào bảng `feedback_history`
  - Thêm cột `image_ids` (TEXT) vào bảng `feedback_history` để lưu JSON array
  - Migrate dữ liệu cũ: copy `note` sang `content` cho backward compatibility

- **Service Logic**:
  - Cải thiện `FeedbackService.processFeedback` để lưu cả `content` và `note`
  - Cải thiện `FeedbackService.createHistory` để hỗ trợ `content`, `note`, và `imageIds`
  - Cải thiện `ProcessFeedbackRequest` để nhận cả `content` và `note`

- **Frontend**:
  - Cải thiện UI timeline để hiển thị riêng "Nội dung xử lý" và "Ghi chú"
  - Thêm hiển thị file đính kèm với link download trong timeline
  - Cải thiện upload process: upload images trước, lấy imageIds, rồi gửi trong request

### Fixed
- **Bug Fixes**:
  - Fix lỗi "Ghi chú" và "File đính kèm" không được lưu vào history
  - Fix lỗi timeline không hiển thị attachments
  - Fix lỗi UI chỉ có 1 trường `note` trong khi có 2 trường riêng biệt

## [1.3.0] - 2025-12-07

### Added
- **Notification System**:
  - Thêm notification type `RATING` cho đánh giá mới
  - Thêm `ratingId` và `feedbackId` vào Notification entity
  - Tạo notification cho ADMIN/LEADER khi có đánh giá mới
  - Tạo notification cho handler khi được phân công
  - Tạo notification cho ADMIN/LEADER khi feedback hoàn thành

- **Rating System**:
  - Hỗ trợ đánh giá theo user (mỗi user có thể đánh giá riêng)
  - Tính toán đánh giá trung bình cho mỗi feedback
  - Kiểm tra user đã đánh giá chưa
  - Validation: chỉ cho phép đánh giá khi feedback status = COMPLETED

- **Feedback Management**:
  - Tự động phân công handler theo phòng ban khi tạo feedback
  - Thêm `receiverId` để lưu người tiếp nhận (người tạo feedback)
  - Endpoint `/feedbacks/by-code/{code}` để tìm feedback bằng code
  - Cải thiện logic generate feedback code với sequence number
  - Sắp xếp feedback theo Ngày nhận và Số PA (descending)

- **API Improvements**:
  - Thêm `receiverName` và `doctorName` vào FeedbackDTO
  - Cải thiện mapping dữ liệu trong ReportService
  - Thêm đầy đủ thông tin cho Dashboard stats

### Changed
- **Database Schema**:
  - Thêm `user_id` vào bảng `ratings` (unique constraint: `feedback_id, user_id`)
  - Thêm `receiver_id` vào bảng `feedbacks`
  - Thêm `feedback_id` và `rating_id` vào bảng `notifications`
  - Cập nhật CHECK constraint cho `notifications.type` để bao gồm `RATING`

- **Service Logic**:
  - Cải thiện `FeedbackService.createFeedback` để tự động assign handler
  - Cải thiện `RatingService` để hỗ trợ multi-user ratings
  - Cải thiện `NotificationService` để tạo notification cho từng user

### Fixed
- **Bug Fixes**:
  - Fix lỗi duplicate key khi tạo feedback code
  - Fix lỗi thiếu dữ liệu trong Dashboard stats
  - Fix lỗi notification không hiển thị do type mismatch
  - Fix lỗi thiếu `doctorName` trong FeedbackDTO
  - Fix lỗi sorting feedback không đúng thứ tự

- **Security**:
  - Thêm `@PreAuthorize` cho NotificationController
  - Cải thiện role-based access control cho Dashboard

## [1.2.0] - 2025-12-06

### Added
- Thêm Rating entity và service
- Thêm Notification entity và service
- Thêm FeedbackHistory để lưu lịch sử xử lý
- Thêm endpoint xử lý feedback

### Changed
- Cải thiện FeedbackService với history tracking
- Cải thiện DTOs với đầy đủ thông tin

## [1.1.0] - 2025-12-05

### Added
- Thêm ReportService với Dashboard stats
- Thêm Department và Doctor management
- Thêm filter và search cho feedback

## [1.0.0] - 2025-12-04

### Added
- Release ban đầu
- Authentication và Authorization với JWT
- CRUD operations cho Feedback
- User, Department, Doctor management
- File upload service

[1.4.2]: https://github.com/your-repo/compare/v1.4.1...v1.4.2
[1.4.1]: https://github.com/your-repo/compare/v1.4.0...v1.4.1
[1.4.0]: https://github.com/your-repo/compare/v1.3.0...v1.4.0
[1.3.0]: https://github.com/your-repo/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/your-repo/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/your-repo/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/your-repo/releases/tag/v1.0.0

