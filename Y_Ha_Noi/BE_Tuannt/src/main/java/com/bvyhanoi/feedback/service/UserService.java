package com.bvyhanoi.feedback.service;

import com.bvyhanoi.feedback.dto.CreateUserRequest;
import com.bvyhanoi.feedback.dto.PagedResponse;
import com.bvyhanoi.feedback.dto.UpdateUserRequest;
import com.bvyhanoi.feedback.dto.UserDTO;
import com.bvyhanoi.feedback.entity.User;
import com.bvyhanoi.feedback.exception.ResourceNotFoundException;
import com.bvyhanoi.feedback.exception.ValidationException;
import com.bvyhanoi.feedback.repository.DepartmentRepository;
import com.bvyhanoi.feedback.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.util.ArrayList;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    @Value("${app.mail.portal-url:http://localhost:8080}")
    private String portalUrl;
    
    public PagedResponse<UserDTO> getAllUsers(int page, int size, String keyword, User.Role role, User.UserStatus status) {
        List<User> allUsers = userRepository.findAll();
        
        // Apply filters
        List<User> filteredUsers = allUsers.stream()
            .filter(u -> keyword == null || keyword.isEmpty() || 
                u.getUsername().toLowerCase().contains(keyword.toLowerCase()) ||
                (u.getFullName() != null && u.getFullName().toLowerCase().contains(keyword.toLowerCase())) ||
                (u.getEmail() != null && u.getEmail().toLowerCase().contains(keyword.toLowerCase())))
            .filter(u -> role == null || u.getRole() == role)
            .filter(u -> status == null || u.getStatus() == status)
            .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt()))
            .collect(Collectors.toList());
        
        // Pagination
        int start = (page - 1) * size;
        int end = Math.min(start + size, filteredUsers.size());
        List<User> pagedUsers = start < filteredUsers.size() 
            ? filteredUsers.subList(start, end) 
            : new ArrayList<>();
        
        List<UserDTO> userDTOs = pagedUsers.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        
        return PagedResponse.of(userDTOs, (long) filteredUsers.size(), page, size);
    }
    
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toDTO(user);
    }
    
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already exists");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already exists");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setDepartmentId(request.getDepartmentId());
        user.setStatus(User.UserStatus.ACTIVE);
        
        return toDTO(userRepository.save(user));
    }
    
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ValidationException("Email already exists");
            }
        }
        
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setDepartmentId(request.getDepartmentId());
        
        return toDTO(userRepository.save(user));
    }
    
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userRepository.delete(user);
    }
    
    @Transactional
    public String resetPassword(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        
        String newPassword = "123456"; // Default password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Gửi mail mật khẩu mới nếu user có email
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            String subject = "Mật khẩu mới cho tài khoản " + user.getUsername();
            String body = """
                    Tài khoản: %s
                    Mật khẩu mới: %s

                    Vui lòng đăng nhập và đổi mật khẩu ngay: %s
                    """.formatted(user.getUsername(), newPassword, portalUrl);
            mailService.sendPlainText(user.getEmail(), subject, body);
        }
        
        return newPassword;
    }
    
    @Transactional
    public UserDTO toggleStatus(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));
        
        user.setStatus(user.getStatus() == User.UserStatus.ACTIVE 
            ? User.UserStatus.INACTIVE 
            : User.UserStatus.ACTIVE);
        
        return toDTO(userRepository.save(user));
    }
    
    public List<UserDTO> getHandlers(Long departmentId) {
        List<User> handlers = userRepository.findAll().stream()
            .filter(u -> u.getRole() == User.Role.HANDLER && u.getStatus() == User.UserStatus.ACTIVE)
            .filter(u -> departmentId == null || u.getDepartmentId() != null && u.getDepartmentId().equals(departmentId))
            .collect(Collectors.toList());
        
        return handlers.stream().map(this::toDTO).collect(Collectors.toList());
    }
    
    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setDepartmentId(user.getDepartmentId());
        dto.setStatus(user.getStatus());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        
        // Load department name
        if (user.getDepartmentId() != null) {
            departmentRepository.findById(user.getDepartmentId())
                .ifPresent(dept -> dto.setDepartmentName(dept.getName()));
        }
        
        return dto;
    }
}

