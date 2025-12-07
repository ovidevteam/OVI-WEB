package com.bvyhanoi.feedback.config;

import com.bvyhanoi.feedback.entity.*;
import com.bvyhanoi.feedback.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Value("${app.data.reset:false}")
    private boolean resetData;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private FeedbackHistoryRepository feedbackHistoryRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        System.out.println("========================================");
        System.out.println("Starting data initialization...");
        System.out.println("========================================");
        
        // Delete demo data (keep users) - only if resetData is true
        if (resetData) {
            System.out.println("⚠️  WARNING: Reset data mode is ENABLED!");
            System.out.println("⚠️  This will DELETE all data from database (except users)!");
            deleteDemoData();
        } else {
            System.out.println("ℹ️  Reset data mode is DISABLED. Skipping data deletion.");
            System.out.println("ℹ️  To enable, set app.data.reset=true in application.yml");
        }
        
        // Create departments first
        List<Department> departments = createDepartments();
        
        // Create doctors
        List<Doctor> doctors = createDoctors(departments);
        
        // Create users (only if not exists)
        List<User> users = createUsers(departments);
        
        // Create feedbacks
        List<Feedback> feedbacks = createFeedbacks(departments, doctors, users);
        
        // Create ratings
        createRatings(users, feedbacks, doctors);
        
        // Create notifications
        createNotifications(users, feedbacks);
        
        System.out.println("========================================");
        System.out.println("✅ Data initialization completed!");
        System.out.println("========================================");
        printLoginInfo();
    }
    
    /**
     * Delete all demo data except users
     * WARNING: This will DELETE data from the REAL database!
     */
    private void deleteDemoData() {
        System.out.println("🗑️  Deleting demo data from database (keeping users)...");
        
        try {
            // Delete in order to respect foreign key constraints
            long historyCount = feedbackHistoryRepository.count();
            long ratingCount = ratingRepository.count();
            long notifCount = notificationRepository.count();
            long feedbackCount = feedbackRepository.count();
            long doctorCount = doctorRepository.count();
            long deptCount = departmentRepository.count();
            
            System.out.println("   - FeedbackHistory: " + historyCount + " records");
            System.out.println("   - Ratings: " + ratingCount + " records");
            System.out.println("   - Notifications: " + notifCount + " records");
            System.out.println("   - Feedbacks: " + feedbackCount + " records");
            System.out.println("   - Doctors: " + doctorCount + " records");
            System.out.println("   - Departments: " + deptCount + " records");
            
            feedbackHistoryRepository.deleteAll();
            ratingRepository.deleteAll();
            notificationRepository.deleteAll();
            feedbackRepository.deleteAll();
            doctorRepository.deleteAll();
            departmentRepository.deleteAll();
            
            System.out.println("✅ Demo data deleted from database (users preserved)");
        } catch (Exception e) {
            System.err.println("❌ Error deleting demo data: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    private List<Department> createDepartments() {
        List<Department> departments = new ArrayList<>();
        
        String[] deptData = {
            "PB-001,Nội khoa,Phòng khám và điều trị các bệnh lý nội khoa, chăm sóc sức khỏe tổng quát cho bệnh nhân,noikhoa@bvyhanoi.vn",
            "PB-002,Ngoại khoa,Phòng khám và điều trị các bệnh lý ngoại khoa, phẫu thuật các ca bệnh cần can thiệp phẫu thuật,ngoaikhoa@bvyhanoi.vn",
            "PB-003,Da liễu,Phòng khám và điều trị các bệnh lý về da, tóc, móng và các bệnh lây truyền qua đường tình dục,dalieu@bvyhanoi.vn",
            "PB-004,Sản khoa,Phòng khám và chăm sóc sức khỏe phụ nữ, thai sản, sản phụ khoa,sankhoa@bvyhanoi.vn",
            "PB-005,Nhi khoa,Phòng khám và điều trị các bệnh lý ở trẻ em từ sơ sinh đến 15 tuổi,nhikhoa@bvyhanoi.vn"
        };
        
        for (String data : deptData) {
            String[] parts = data.split(",");
            String code = parts[0];
            
            // Check if department already exists
            Department existingDept = departmentRepository.findByCode(code).orElse(null);
            if (existingDept != null) {
                departments.add(existingDept);
                continue;
            }
            
                Department dept = new Department();
                dept.setCode(code);
                dept.setName(parts[1]);
            dept.setDescription(parts[2]);
                dept.setNotificationEmail(parts[3]);
                dept.setStatus(Department.DepartmentStatus.ACTIVE);
                departments.add(departmentRepository.save(dept));
        }
        
        System.out.println("✅ Checked/created " + departments.size() + " departments");
        return departments;
    }
    
    private List<Doctor> createDoctors(List<Department> departments) {
        List<Doctor> doctors = new ArrayList<>();
        
        String[] doctorData = {
            "BS-001,BS. Nguyễn Văn A,Nội khoa,bsa@bvyhanoi.vn,0912345678,0",
            "BS-002,BS. Trần Thị B,Ngoại khoa,bsb@bvyhanoi.vn,0987654321,1",
            "BS-003,BS. Lê Văn C,Da liễu,bsc@bvyhanoi.vn,0909090909,2",
            "BS-004,BS. Phạm Thị D,Sản khoa,bsd@bvyhanoi.vn,0908080808,3",
            "BS-005,BS. Hoàng Văn E,Nhi khoa,bse@bvyhanoi.vn,0907070707,4",
            "BS-006,BS. Nguyễn Thị F,Nội khoa,bsf@bvyhanoi.vn,0906060606,0",
            "BS-007,BS. Trần Văn G,Ngoại khoa,bsg@bvyhanoi.vn,0905050505,1",
            "BS-008,BS. Lê Thị H,Da liễu,bsh@bvyhanoi.vn,0904040404,2",
            "BS-009,BS. Phạm Văn I,Sản khoa,bsi@bvyhanoi.vn,0903030303,3",
            "BS-010,BS. Hoàng Thị J,Nhi khoa,bsj@bvyhanoi.vn,0902020202,4"
        };
        
        for (String data : doctorData) {
            String[] parts = data.split(",");
            String code = parts[0];
            
            // Check if doctor already exists
            Doctor existingDoctor = doctorRepository.findByCode(code).orElse(null);
            if (existingDoctor != null) {
                doctors.add(existingDoctor);
                continue;
            }
            
                Doctor doctor = new Doctor();
                doctor.setCode(code);
                doctor.setFullName(parts[1]);
                doctor.setSpecialty(parts[2]);
                doctor.setDepartmentId(departments.get(Integer.parseInt(parts[5])).getId());
                doctor.setEmail(parts[3]);
                doctor.setPhone(parts[4]);
                doctor.setStatus(Doctor.DoctorStatus.ACTIVE);
                doctors.add(doctorRepository.save(doctor));
        }
        
        System.out.println("✅ Checked/created " + doctors.size() + " doctors");
        return doctors;
    }
    
    private List<User> createUsers(List<Department> departments) {
        List<User> users = new ArrayList<>();
        
        // Admin
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Quản trị viên");
            admin.setEmail("admin@bvyhanoi.vn");
            admin.setPhone("0900000001");
            admin.setRole(User.Role.ADMIN);
            admin.setStatus(User.UserStatus.ACTIVE);
            users.add(userRepository.save(admin));
        } else {
            users.add(userRepository.findByUsername("admin").get());
        }
        
        // Leader
        if (userRepository.findByUsername("leader").isEmpty()) {
            User leader = new User();
            leader.setUsername("leader");
            leader.setPassword(passwordEncoder.encode("leader123"));
            leader.setFullName("Nguyễn Văn Lãnh đạo");
            leader.setEmail("leader@bvyhanoi.vn");
            leader.setPhone("0900000002");
            leader.setRole(User.Role.LEADER);
            leader.setStatus(User.UserStatus.ACTIVE);
            users.add(userRepository.save(leader));
        } else {
            users.add(userRepository.findByUsername("leader").get());
        }
        
        // Receiver
        if (userRepository.findByUsername("receiver").isEmpty()) {
            User receiver = new User();
            receiver.setUsername("receiver");
            receiver.setPassword(passwordEncoder.encode("receiver123"));
            receiver.setFullName("Trần Thị Tiếp nhận");
            receiver.setEmail("receiver@bvyhanoi.vn");
            receiver.setPhone("0900000003");
            receiver.setRole(User.Role.RECEIVER);
            receiver.setStatus(User.UserStatus.ACTIVE);
            receiver.setDepartmentId(departments.get(0).getId());
            users.add(userRepository.save(receiver));
        } else {
            users.add(userRepository.findByUsername("receiver").get());
        }
        
        // Handlers
        String[] handlerPhones = {"0900000011", "0900000012", "0900000013", "0900000014", "0900000015"};
        for (int i = 0; i < Math.min(5, departments.size()); i++) {
            String username = "handler" + (i + 1);
            if (userRepository.findByUsername(username).isEmpty()) {
                User handler = new User();
                handler.setUsername(username);
                handler.setPassword(passwordEncoder.encode("handler123"));
                handler.setFullName("BS. Handler " + (i + 1));
                handler.setEmail("handler" + (i + 1) + "@bvyhanoi.vn");
                handler.setPhone(handlerPhones[i]);
                handler.setRole(User.Role.HANDLER);
                handler.setDepartmentId(departments.get(i).getId());
                handler.setStatus(User.UserStatus.ACTIVE);
                users.add(userRepository.save(handler));
            } else {
                users.add(userRepository.findByUsername(username).get());
            }
        }
        
        System.out.println("✅ Users checked/created: " + users.size());
        return users;
    }
    
    private List<Feedback> createFeedbacks(List<Department> departments, List<Doctor> doctors, List<User> users) {
        List<Feedback> feedbacks = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        String[] feedbackContents = {
            "Thời gian chờ khám quá lâu, bệnh nhân phải đợi hơn 2 tiếng",
            "Nhân viên lễ tân thiếu thân thiện với bệnh nhân cao tuổi",
            "Phòng khám thiếu sạch sẽ, cần cải thiện vệ sinh",
            "Khen ngợi bác sĩ điều trị nhiệt tình, chuyên nghiệp",
            "Cơ sở vật chất xuống cấp, máy lạnh không hoạt động",
            "Thái độ phục vụ của nhân viên y tế cần được cải thiện",
            "Bác sĩ giải thích rõ ràng về tình trạng bệnh và phác đồ điều trị",
            "Phản hồi nhanh chóng khi bệnh nhân có thắc mắc về thuốc",
            "Cần cải thiện thái độ phục vụ trong giờ cao điểm",
            "Thời gian chờ khám quá lâu tại phòng khám da liễu"
        };
        
        Feedback.FeedbackLevel[] levels = {
            Feedback.FeedbackLevel.HIGH, Feedback.FeedbackLevel.MEDIUM, Feedback.FeedbackLevel.MEDIUM,
            Feedback.FeedbackLevel.LOW, Feedback.FeedbackLevel.HIGH, Feedback.FeedbackLevel.MEDIUM,
            Feedback.FeedbackLevel.LOW, Feedback.FeedbackLevel.LOW, Feedback.FeedbackLevel.MEDIUM,
            Feedback.FeedbackLevel.HIGH
        };
        
        Feedback.FeedbackStatus[] statuses = {
            Feedback.FeedbackStatus.PROCESSING, Feedback.FeedbackStatus.NEW, Feedback.FeedbackStatus.PROCESSING,
            Feedback.FeedbackStatus.COMPLETED, Feedback.FeedbackStatus.NEW, Feedback.FeedbackStatus.COMPLETED,
            Feedback.FeedbackStatus.COMPLETED, Feedback.FeedbackStatus.COMPLETED, Feedback.FeedbackStatus.PROCESSING,
            Feedback.FeedbackStatus.PROCESSING
        };
        
        for (int i = 0; i < feedbackContents.length; i++) {
            String code = "PA-" + today.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%03d", i + 1);
            
            if (feedbackRepository.findByCode(code).isEmpty()) {
                Feedback feedback = new Feedback();
                feedback.setCode(code);
                feedback.setContent(feedbackContents[i]);
                feedback.setChannel(Feedback.FeedbackChannel.values()[i % 4]);
                feedback.setLevel(levels[i]);
                feedback.setStatus(statuses[i]);
                feedback.setDepartmentId(departments.get(i % departments.size()).getId());
                feedback.setDoctorId(doctors.get(i % doctors.size()).getId());
                feedback.setReceivedDate(today.minusDays(i));
                
                if (statuses[i] == Feedback.FeedbackStatus.COMPLETED) {
                    feedback.setCompletedDate(today.minusDays(i).plusDays(2));
                }
                
                if (statuses[i] == Feedback.FeedbackStatus.PROCESSING || statuses[i] == Feedback.FeedbackStatus.COMPLETED) {
                    User handler = users.stream()
                        .filter(u -> u.getRole() == User.Role.HANDLER)
                        .findFirst()
                        .orElse(null);
                    if (handler != null) {
                        feedback.setHandlerId(handler.getId());
                    }
                }
                
                feedback = feedbackRepository.save(feedback);
                feedbacks.add(feedback);
                
                // Create history
                FeedbackHistory history = new FeedbackHistory();
                history.setFeedbackId(feedback.getId());
                history.setStatus(feedback.getStatus());
                history.setNote("Phản ánh được tạo");
                history.setCreatedBy(users.get(0).getId());
                feedbackHistoryRepository.save(history);
            }
        }
        
        return feedbacks;
    }
    
    private void createRatings(List<User> users, List<Feedback> feedbacks, List<Doctor> doctors) {
        List<Feedback> completedFeedbacks = feedbacks.stream()
            .filter(f -> f.getStatus() == Feedback.FeedbackStatus.COMPLETED)
            .limit(5)
            .toList();
        
        Integer[] ratings = {5, 4, 5, 4, 5};
        String[] comments = {
            "Bác sĩ rất nhiệt tình và chuyên nghiệp",
            "Thái độ phục vụ tốt",
            "Giải thích rõ ràng, dễ hiểu",
            "Điều trị hiệu quả",
            "Rất hài lòng với dịch vụ"
        };
        
        // Get first admin user for demo ratings
        User adminUser = users.stream()
            .filter(u -> u.getRole() == User.Role.ADMIN)
            .findFirst()
            .orElse(users.isEmpty() ? null : users.get(0));
        
        if (adminUser == null) {
            return; // No user to assign rating
        }
        
        for (int i = 0; i < completedFeedbacks.size(); i++) {
            Feedback feedback = completedFeedbacks.get(i);
            // Check if admin already rated this feedback
            if (ratingRepository.findByFeedbackIdAndUserId(feedback.getId(), adminUser.getId()).isEmpty()) {
                Rating rating = new Rating();
                rating.setFeedbackId(feedback.getId());
                rating.setUserId(adminUser.getId());
                rating.setDoctorId(feedback.getDoctorId());
                rating.setRating(ratings[i]);
                rating.setComment(comments[i]);
                ratingRepository.save(rating);
            }
        }
    }
    
    private void createNotifications(List<User> users, List<Feedback> feedbacks) {
        User admin = users.stream().filter(u -> u.getRole() == User.Role.ADMIN).findFirst().orElse(null);
        User leader = users.stream().filter(u -> u.getRole() == User.Role.LEADER).findFirst().orElse(null);
        
        if (admin == null || leader == null) return;
        
        // Create some notifications
        for (int i = 0; i < Math.min(5, feedbacks.size()); i++) {
            Feedback feedback = feedbacks.get(i);
            
            Notification notif1 = new Notification();
            notif1.setUserId(admin.getId());
            notif1.setType(Notification.NotificationType.FEEDBACK);
            notif1.setTitle("Phản ánh mới");
            notif1.setMessage("Phản ánh " + feedback.getCode() + " cần xử lý");
            notif1.setRead(i % 2 == 0);
            notificationRepository.save(notif1);
            
            Notification notif2 = new Notification();
            notif2.setUserId(leader.getId());
            notif2.setType(Notification.NotificationType.FEEDBACK);
            notif2.setTitle("Phản ánh mới");
            notif2.setMessage("Phản ánh " + feedback.getCode() + " từ khoa " + feedback.getDepartmentId());
            notif2.setRead(i % 3 == 0);
            notificationRepository.save(notif2);
        }
    }
    
    private void printLoginInfo() {
        System.out.println("\n📋 Login Credentials:");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("👤 admin / admin123 (ADMIN)");
        System.out.println("👤 leader / leader123 (LEADER)");
        System.out.println("👤 receiver / receiver123 (RECEIVER)");
        System.out.println("👤 handler1 / handler123 (HANDLER)");
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
    }
}
