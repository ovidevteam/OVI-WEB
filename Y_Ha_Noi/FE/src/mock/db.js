/**
 * Mock Database - Demo Data
 * Tất cả mock data cho chế độ DEMO_MODE được tập trung tại đây
 */

export const mockUsers = [
	{ id: 1, username: 'admin', fullName: 'Quản trị viên', email: 'admin@bvyhanoi.vn', role: 'ADMIN', departmentName: '', status: 'ACTIVE' },
	{ id: 2, username: 'leader', fullName: 'Nguyễn Văn Lãnh đạo', email: 'leader@bvyhanoi.vn', role: 'LEADER', departmentName: '', status: 'ACTIVE' },
	{ id: 3, username: 'receiver', fullName: 'Trần Thị Tiếp nhận', email: 'receiver@bvyhanoi.vn', role: 'RECEIVER', departmentName: 'Tiếp nhận', status: 'ACTIVE' },
	{ id: 4, username: 'handler1', fullName: 'BS. Nguyễn Văn A', email: 'handler1@bvyhanoi.vn', role: 'HANDLER', departmentName: 'Nội khoa', status: 'ACTIVE' },
	{ id: 5, username: 'handler2', fullName: 'BS. Trần Thị B', email: 'handler2@bvyhanoi.vn', role: 'HANDLER', departmentName: 'Ngoại khoa', status: 'ACTIVE' }
]

export const mockDepartments = [
	{ id: 1, code: 'PB-001', name: 'Nội khoa', managerName: 'BS. Nguyễn Văn A', defaultHandlerName: 'BS. Nguyễn Văn A', notificationEmail: 'noikhoa@bvyhanoi.vn', status: 'ACTIVE' },
	{ id: 2, code: 'PB-002', name: 'Ngoại khoa', managerName: 'BS. Trần Văn B', defaultHandlerName: 'BS. Trần Văn B', notificationEmail: 'ngoaikhoa@bvyhanoi.vn', status: 'ACTIVE' },
	{ id: 3, code: 'PB-003', name: 'Da liễu', managerName: 'BS. Lê Thị C', defaultHandlerName: 'BS. Lê Thị C', notificationEmail: 'dalieu@bvyhanoi.vn', status: 'ACTIVE' },
	{ id: 4, code: 'PB-004', name: 'Sản khoa', managerName: 'BS. Phạm Thị D', defaultHandlerName: 'BS. Phạm Thị D', notificationEmail: 'sankhoa@bvyhanoi.vn', status: 'ACTIVE' },
	{ id: 5, code: 'PB-005', name: 'Nhi khoa', managerName: 'BS. Hoàng Văn E', defaultHandlerName: 'BS. Hoàng Văn E', notificationEmail: 'nhikhoa@bvyhanoi.vn', status: 'ACTIVE' }
]

export const mockDepartmentsSimple = [
	{ id: 1, name: 'Nội khoa' },
	{ id: 2, name: 'Ngoại khoa' },
	{ id: 3, name: 'Da liễu' },
	{ id: 4, name: 'Sản khoa' },
	{ id: 5, name: 'Nhi khoa' }
]

export const mockDoctors = [
	{ id: 1, code: 'BS-001', fullName: 'BS. Nguyễn Văn A', specialty: 'Nội khoa', departmentName: 'Nội khoa', email: 'bsa@bvyhanoi.vn', phone: '0912345678', status: 'ACTIVE' },
	{ id: 2, code: 'BS-002', fullName: 'BS. Trần Thị B', specialty: 'Ngoại khoa', departmentName: 'Ngoại khoa', email: 'bsb@bvyhanoi.vn', phone: '0987654321', status: 'ACTIVE' },
	{ id: 3, code: 'BS-003', fullName: 'BS. Lê Văn C', specialty: 'Da liễu', departmentName: 'Da liễu', email: 'bsc@bvyhanoi.vn', phone: '0909090909', status: 'ACTIVE' },
	{ id: 4, code: 'BS-004', fullName: 'BS. Phạm Thị D', specialty: 'Sản khoa', departmentName: 'Sản khoa', email: 'bsd@bvyhanoi.vn', phone: '0908080808', status: 'ACTIVE' },
	{ id: 5, code: 'BS-005', fullName: 'BS. Hoàng Văn E', specialty: 'Nhi khoa', departmentName: 'Nhi khoa', email: 'bse@bvyhanoi.vn', phone: '0907070707', status: 'ACTIVE' }
]

export const mockFeedbacks = [
	{ id: 1, code: 'PA-2024-001', receivedDate: '2024-11-27', content: 'Thời gian chờ khám quá lâu, bệnh nhân phải đợi hơn 2 tiếng', departmentName: 'Nội khoa', level: 'HIGH', status: 'PROCESSING', handlerName: 'BS. Nguyễn Văn A' },
	{ id: 2, code: 'PA-2024-002', receivedDate: '2024-11-26', content: 'Nhân viên lễ tân thiếu thân thiện với bệnh nhân cao tuổi', departmentName: 'Ngoại khoa', level: 'MEDIUM', status: 'NEW', handlerName: null },
	{ id: 3, code: 'PA-2024-003', receivedDate: '2024-11-25', content: 'Phòng khám thiếu sạch sẽ, cần cải thiện vệ sinh', departmentName: 'Da liễu', level: 'MEDIUM', status: 'PROCESSING', handlerName: 'Trần Thị B' },
	{ id: 4, code: 'PA-2024-004', receivedDate: '2024-11-24', content: 'Khen ngợi bác sĩ điều trị nhiệt tình, chuyên nghiệp', departmentName: 'Sản khoa', level: 'LOW', status: 'COMPLETED', handlerName: 'BS. Lê Văn C' },
	{ id: 5, code: 'PA-2024-005', receivedDate: '2024-11-23', content: 'Cơ sở vật chất xuống cấp, máy lạnh không hoạt động', departmentName: 'Nhi khoa', level: 'HIGH', status: 'NEW', handlerName: null },
	{ id: 6, code: 'PA-2024-006', receivedDate: '2024-11-22', content: 'Thái độ phục vụ của nhân viên y tế cần được cải thiện', departmentName: 'Nội khoa', level: 'MEDIUM', status: 'COMPLETED', handlerName: 'BS. Nguyễn Văn A' },
	{ id: 7, code: 'PA-2024-007', receivedDate: '2024-11-21', content: 'Bác sĩ giải thích rõ ràng về tình trạng bệnh và phác đồ điều trị', departmentName: 'Ngoại khoa', level: 'LOW', status: 'COMPLETED', handlerName: 'BS. Trần Thị B' },
	{ id: 8, code: 'PA-2024-008', receivedDate: '2024-11-20', content: 'Phản hồi nhanh chóng khi bệnh nhân có thắc mắc về thuốc', departmentName: 'Da liễu', level: 'LOW', status: 'COMPLETED', handlerName: 'BS. Lê Văn C' },
	{ id: 9, code: 'PA-2024-009', receivedDate: '2024-11-19', content: 'Cần cải thiện thái độ phục vụ trong giờ cao điểm', departmentName: 'Sản khoa', level: 'MEDIUM', status: 'PROCESSING', handlerName: 'BS. Phạm Thị D' },
	{ id: 10, code: 'PA-2024-010', receivedDate: '2024-11-18', content: 'Thời gian chờ khám quá lâu tại phòng khám da liễu', departmentName: 'Da liễu', level: 'HIGH', status: 'PROCESSING', handlerName: 'BS. Lê Văn C' }
]

export const mockDashboardStats = {
	total: 156,
	processing: 24,
	completed: 121,
	overdue: 3
}

export const mockMonthlyStats = [12, 15, 18, 14, 16, 20, 22, 19, 17, 15, 18, 16]

export const mockDepartmentStats = [
	{ departmentName: 'Nội khoa', count: 45 },
	{ departmentName: 'Ngoại khoa', count: 38 },
	{ departmentName: 'Da liễu', count: 25 },
	{ departmentName: 'Sản khoa', count: 30 },
	{ departmentName: 'Nhi khoa', count: 22 }
]

export const mockReportByDepartment = [
	{ id: 1, departmentName: 'Nội khoa', total: 45, pending: 5, processing: 8, completed: 32, overdue: 2, avgDays: 2.3, completionRate: 71 },
	{ id: 2, departmentName: 'Ngoại khoa', total: 38, pending: 3, processing: 5, completed: 30, overdue: 1, avgDays: 1.8, completionRate: 79 },
	{ id: 3, departmentName: 'Da liễu', total: 25, pending: 2, processing: 3, completed: 20, overdue: 0, avgDays: 1.5, completionRate: 80 },
	{ id: 4, departmentName: 'Sản khoa', total: 30, pending: 4, processing: 6, completed: 20, overdue: 3, avgDays: 2.8, completionRate: 67 },
	{ id: 5, departmentName: 'Nhi khoa', total: 22, pending: 1, processing: 2, completed: 19, overdue: 0, avgDays: 1.2, completionRate: 86 }
]

export const mockReportByDoctor = [
	{ id: 1, doctorName: 'BS. Nguyễn Văn A', departmentName: 'Nội khoa', specialty: 'Nội khoa', total: 25, completed: 23, avgDays: 1.2, rating: 4.8 },
	{ id: 2, doctorName: 'BS. Trần Thị B', departmentName: 'Ngoại khoa', specialty: 'Ngoại khoa', total: 20, completed: 18, avgDays: 1.5, rating: 4.5 },
	{ id: 3, doctorName: 'BS. Lê Văn C', departmentName: 'Da liễu', specialty: 'Da liễu', total: 18, completed: 17, avgDays: 1.8, rating: 4.6 },
	{ id: 4, doctorName: 'BS. Phạm Thị D', departmentName: 'Sản khoa', specialty: 'Sản khoa', total: 15, completed: 12, avgDays: 2.5, rating: 4.2 },
	{ id: 5, doctorName: 'BS. Hoàng Văn E', departmentName: 'Nhi khoa', specialty: 'Nhi khoa', total: 22, completed: 21, avgDays: 1.0, rating: 4.9 }
]

export const mockReportWithImages = [
	{
		id: 1,
		code: 'PA-20251127-001',
		content: 'Phản ánh về thái độ phục vụ của nhân viên khoa Nội khi tiếp nhận bệnh nhân',
		departmentName: 'Nội khoa',
		receivedDate: '2025-11-27',
		status: 'PROCESSING',
		images: [
			{ id: 1, url: 'https://via.placeholder.com/300x200?text=Image+1' },
			{ id: 2, url: 'https://via.placeholder.com/300x200?text=Image+2' }
		]
	},
	{
		id: 2,
		code: 'PA-20251126-002',
		content: 'Thời gian chờ khám quá lâu tại phòng khám da liễu, cần cải thiện',
		departmentName: 'Da liễu',
		receivedDate: '2025-11-26',
		status: 'COMPLETED',
		images: [
			{ id: 3, url: 'https://via.placeholder.com/300x200?text=Image+3' },
			{ id: 4, url: 'https://via.placeholder.com/300x200?text=Image+4' },
			{ id: 5, url: 'https://via.placeholder.com/300x200?text=Image+5' }
		]
	},
	{
		id: 3,
		code: 'PA-20251125-003',
		content: 'Cơ sở vật chất phòng khám cần được nâng cấp để phục vụ bệnh nhân tốt hơn',
		departmentName: 'Ngoại khoa',
		receivedDate: '2025-11-25',
		status: 'NEW',
		images: [
			{ id: 6, url: 'https://via.placeholder.com/300x200?text=Image+6' }
		]
	}
]

export const mockNotifications = [
	{
		id: 1,
		type: 'feedback',
		title: 'Phản ánh mới',
		message: 'PA-20251127-001 cần xử lý gấp',
		time: '5 phút trước',
		read: false
	},
	{
		id: 2,
		type: 'assigned',
		title: 'Được phân công',
		message: 'Bạn được phân công xử lý PA-20251127-002',
		time: '1 giờ trước',
		read: false
	},
	{
		id: 3,
		type: 'completed',
		title: 'Hoàn thành',
		message: 'PA-20251126-003 đã được xử lý xong',
		time: '2 giờ trước',
		read: true
	},
	{
		id: 4,
		type: 'feedback',
		title: 'Phản ánh mới',
		message: 'PA-20251125-004 từ khoa Ngoại',
		time: '1 ngày trước',
		read: true
	}
]

export const mockFeedbackStats = {
	total: 23,
	pending: 15,
	myPending: 5,
	needRating: 8
}

export const mockDoctorFeedbacks = [
	{ id: 1, code: 'PA-2024-001', content: 'Thời gian chờ khám quá lâu, bệnh nhân phải đợi hơn 2 tiếng', status: 'COMPLETED', processingTime: 1.5, completedDate: '20/11/2024' },
	{ id: 2, code: 'PA-2024-004', content: 'Khen ngợi bác sĩ điều trị nhiệt tình, chuyên nghiệp', status: 'COMPLETED', processingTime: 0.5, completedDate: '22/11/2024' },
	{ id: 3, code: 'PA-2024-007', content: 'Bác sĩ giải thích rõ ràng về tình trạng bệnh và phác đồ điều trị', status: 'COMPLETED', processingTime: 1.0, completedDate: '24/11/2024' },
	{ id: 4, code: 'PA-2024-010', content: 'Phản hồi nhanh chóng khi bệnh nhân có thắc mắc về thuốc', status: 'COMPLETED', processingTime: 2.0, completedDate: '25/11/2024' },
	{ id: 5, code: 'PA-2024-015', content: 'Cần cải thiện thái độ phục vụ trong giờ cao điểm', status: 'PROCESSING', processingTime: 1.5, completedDate: null }
]

export const mockDepartmentFeedbacks = [
	{ id: 1, code: 'PA-2024-001', content: 'Thời gian chờ khám quá lâu, bệnh nhân phải đợi hơn 2 tiếng', status: 'COMPLETED', createdDate: '15/11/2024', handlerName: 'BS. Nguyễn Văn A' },
	{ id: 2, code: 'PA-2024-002', content: 'Nhân viên lễ tân thiếu thân thiện với bệnh nhân cao tuổi', status: 'PROCESSING', createdDate: '16/11/2024', handlerName: 'Trần Thị B' },
	{ id: 3, code: 'PA-2024-003', content: 'Phòng khám thiếu sạch sẽ, cần cải thiện vệ sinh', status: 'PENDING', createdDate: '17/11/2024', handlerName: null },
	{ id: 4, code: 'PA-2024-004', content: 'Khen ngợi bác sĩ điều trị nhiệt tình, chuyên nghiệp', status: 'COMPLETED', createdDate: '18/11/2024', handlerName: 'BS. Lê Văn C' },
	{ id: 5, code: 'PA-2024-005', content: 'Cơ sở vật chất xuống cấp, máy lạnh không hoạt động', status: 'PROCESSING', createdDate: '19/11/2024', handlerName: 'Phạm Văn D' }
]

export const mockProcessHistory = [
	{ id: 1, action: 'ASSIGNED', description: 'Được phân công cho BS. Nguyễn Văn A', createdBy: 'Admin', createdDate: '2024-11-15T10:00:00' },
	{ id: 2, action: 'PROCESSING', description: 'Đang xử lý phản ánh', createdBy: 'BS. Nguyễn Văn A', createdDate: '2024-11-15T14:30:00' },
	{ id: 3, action: 'COMPLETED', description: 'Đã hoàn thành xử lý', createdBy: 'BS. Nguyễn Văn A', createdDate: '2024-11-16T09:00:00' }
]

export const mockRatings = [
	{ id: 1, feedbackId: 1, doctorId: 1, doctorName: 'BS. Nguyễn Văn A', rating: 5, comment: 'Bác sĩ rất nhiệt tình và chuyên nghiệp' },
	{ id: 2, feedbackId: 2, doctorId: 2, doctorName: 'BS. Trần Thị B', rating: 4, comment: 'Thái độ phục vụ tốt' },
	{ id: 3, feedbackId: 3, doctorId: 3, doctorName: 'BS. Lê Văn C', rating: 5, comment: 'Giải thích rõ ràng, dễ hiểu' }
]

