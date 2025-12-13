package com.bvyhanoi.feedback.service;

import com.bvyhanoi.feedback.dto.DashboardStatsDTO;
import com.bvyhanoi.feedback.dto.FeedbackDTO;
import com.bvyhanoi.feedback.dto.FeedbackImageDTO;
import com.bvyhanoi.feedback.entity.Feedback;
import com.bvyhanoi.feedback.entity.FeedbackImage;
import com.bvyhanoi.feedback.repository.DepartmentRepository;
import com.bvyhanoi.feedback.repository.DoctorRepository;
import com.bvyhanoi.feedback.repository.FeedbackImageRepository;
import com.bvyhanoi.feedback.repository.FeedbackRepository;
import com.bvyhanoi.feedback.repository.RatingRepository;
import com.bvyhanoi.feedback.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    
    @Autowired
    private FeedbackRepository feedbackRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private DoctorRepository doctorRepository;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FeedbackImageRepository feedbackImageRepository;
    
    public DashboardStatsDTO getDashboardStats() {
        List<Feedback> allFeedbacks = feedbackRepository.findAll();
        
        DashboardStatsDTO.Stats stats = new DashboardStatsDTO.Stats();
        stats.setTotal((long) allFeedbacks.size());
        stats.setPending(allFeedbacks.stream().filter(f -> f.getStatus() == Feedback.FeedbackStatus.NEW).count());
        stats.setProcessing(allFeedbacks.stream().filter(f -> f.getStatus() == Feedback.FeedbackStatus.PROCESSING || f.getStatus() == Feedback.FeedbackStatus.ASSIGNED).count());
        stats.setCompleted(allFeedbacks.stream().filter(f -> f.getStatus() == Feedback.FeedbackStatus.COMPLETED).count());
        stats.setOverdue(0L); // TODO: Calculate overdue
        
        // Monthly stats for current year
        int currentYear = LocalDate.now().getYear();
        List<DashboardStatsDTO.MonthlyStat> monthlyStats = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(currentYear, month);
            LocalDate start = yearMonth.atDay(1);
            LocalDate end = yearMonth.atEndOfMonth();
            
            long count = allFeedbacks.stream()
                .filter(f -> f.getReceivedDate() != null 
                    && !f.getReceivedDate().isBefore(start) 
                    && !f.getReceivedDate().isAfter(end))
                .count();
            
            monthlyStats.add(new DashboardStatsDTO.MonthlyStat(month, count));
        }
        
        // Department stats (top 5)
        Map<Long, Long> deptCounts = allFeedbacks.stream()
            .filter(f -> f.getDepartmentId() != null)
            .collect(Collectors.groupingBy(Feedback::getDepartmentId, Collectors.counting()));
        
        List<DashboardStatsDTO.DepartmentStat> departmentStats = deptCounts.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .limit(5)
            .map(e -> {
                Long deptId = e.getKey();
                String deptName = departmentRepository.findById(deptId)
                    .map(dept -> dept.getName())
                    .orElse("Department " + deptId);
                return new DashboardStatsDTO.DepartmentStat(deptName, e.getValue());
            })
            .collect(Collectors.toList());
        
        // Recent feedbacks - sort by code DESC
        List<Feedback> allRecentFeedbacks = feedbackRepository.findAll();
        List<Feedback> recentFeedbacks = allRecentFeedbacks.stream()
            .sorted((f1, f2) -> {
                // Sort by code DESC
                return f2.getCode().compareTo(f1.getCode());
            })
            .limit(5)
            .collect(Collectors.toList());
        
        List<FeedbackDTO> recentDTOs = recentFeedbacks.stream()
            .map(f -> {
                FeedbackDTO dto = new FeedbackDTO();
                dto.setId(f.getId());
                dto.setCode(f.getCode());
                dto.setContent(f.getContent());
                dto.setChannel(f.getChannel());
                dto.setLevel(f.getLevel());
                dto.setStatus(f.getStatus());
                dto.setReceivedDate(f.getReceivedDate());
                dto.setDepartmentId(f.getDepartmentId());
                dto.setDoctorId(f.getDoctorId());
                dto.setHandlerId(f.getHandlerId());
                
                // Map department name
                if (f.getDepartmentId() != null) {
                    departmentRepository.findById(f.getDepartmentId())
                        .ifPresent(dept -> dto.setDepartmentName(dept.getName()));
                }
                
                // Map doctor name
                if (f.getDoctorId() != null) {
                    doctorRepository.findById(f.getDoctorId())
                        .ifPresent(doctor -> dto.setDoctorName(doctor.getFullName()));
                }
                
                // Map handler name
                if (f.getHandlerId() != null) {
                    userRepository.findById(f.getHandlerId())
                        .ifPresent(handler -> dto.setHandlerName(handler.getFullName()));
                }
                
                // Map receiver name
                if (f.getReceiverId() != null) {
                    dto.setReceiverId(f.getReceiverId());
                    userRepository.findById(f.getReceiverId())
                        .ifPresent(receiver -> dto.setReceiverName(receiver.getFullName()));
                }
                
                return dto;
            })
            .collect(Collectors.toList());
        
        DashboardStatsDTO result = new DashboardStatsDTO();
        result.setStats(stats);
        result.setMonthlyStats(monthlyStats);
        result.setDepartmentStats(departmentStats);
        result.setRecentFeedbacks(recentDTOs);
        
        return result;
    }
    
    public List<DashboardStatsDTO.MonthlyStat> getMonthlyStats(Integer year) {
        if (year == null) {
            year = LocalDate.now().getYear();
        }
        
        List<Feedback> allFeedbacks = feedbackRepository.findAll();
        List<DashboardStatsDTO.MonthlyStat> monthlyStats = new ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            LocalDate start = yearMonth.atDay(1);
            LocalDate end = yearMonth.atEndOfMonth();
            
            long count = allFeedbacks.stream()
                .filter(f -> f.getReceivedDate() != null 
                    && !f.getReceivedDate().isBefore(start) 
                    && !f.getReceivedDate().isAfter(end))
                .count();
            
            monthlyStats.add(new DashboardStatsDTO.MonthlyStat(month, count));
        }
        
        return monthlyStats;
    }
    
    public List<DashboardStatsDTO.DepartmentStat> getDepartmentStats(LocalDate dateFrom, LocalDate dateTo, Long departmentId, Integer limit) {
        List<Feedback> allFeedbacks = feedbackRepository.findAll();
        
        List<Feedback> filteredFeedbacks = allFeedbacks.stream()
            .filter(f -> dateFrom == null || f.getReceivedDate() == null || !f.getReceivedDate().isBefore(dateFrom))
            .filter(f -> dateTo == null || f.getReceivedDate() == null || !f.getReceivedDate().isAfter(dateTo))
            .filter(f -> departmentId == null || f.getDepartmentId() == null || f.getDepartmentId().equals(departmentId))
            .collect(Collectors.toList());
        
        Map<Long, Long> deptCounts = filteredFeedbacks.stream()
            .filter(f -> f.getDepartmentId() != null)
            .collect(Collectors.groupingBy(Feedback::getDepartmentId, Collectors.counting()));
        
        return deptCounts.entrySet().stream()
            .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
            .limit(limit != null ? limit : 10)
            .map(e -> {
                Long deptId = e.getKey();
                String deptName = departmentRepository.findById(deptId)
                    .map(dept -> dept.getName())
                    .orElse("Department " + deptId);
                return new DashboardStatsDTO.DepartmentStat(deptName, e.getValue());
            })
            .collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getByDepartment(LocalDate dateFrom, LocalDate dateTo, Long departmentId) {
        List<Feedback> allFeedbacks = feedbackRepository.findAll();
        
        List<Feedback> filteredFeedbacks = allFeedbacks.stream()
            .filter(f -> dateFrom == null || f.getReceivedDate() == null || !f.getReceivedDate().isBefore(dateFrom))
            .filter(f -> dateTo == null || f.getReceivedDate() == null || !f.getReceivedDate().isAfter(dateTo))
            .filter(f -> departmentId == null || f.getDepartmentId() == null || f.getDepartmentId().equals(departmentId))
            .collect(Collectors.toList());
        
        Map<Long, List<Feedback>> byDept = filteredFeedbacks.stream()
            .filter(f -> f.getDepartmentId() != null)
            .collect(Collectors.groupingBy(Feedback::getDepartmentId));
        
        LocalDate today = LocalDate.now();
        final int OVERDUE_DAYS = 7; // Feedback quá 7 ngày chưa complete được coi là overdue
        
        return byDept.entrySet().stream().map(e -> {
            Long deptId = e.getKey();
            List<Feedback> deptFeedbacks = e.getValue();
            
            Map<String, Object> deptData = new HashMap<>();
            deptData.put("id", deptId);
            
            // Get actual department name from repository
            final String[] deptName = {"Department " + deptId};
            departmentRepository.findById(deptId).ifPresent(dept -> {
                deptName[0] = dept.getName();
            });
            deptData.put("departmentName", deptName[0]);
            
            long total = deptFeedbacks.size();
            long pending = deptFeedbacks.stream()
                .filter(f -> f.getStatus() == Feedback.FeedbackStatus.NEW)
                .count();
            long processing = deptFeedbacks.stream()
                .filter(f -> f.getStatus() == Feedback.FeedbackStatus.PROCESSING || f.getStatus() == Feedback.FeedbackStatus.ASSIGNED)
                .count();
            long completed = deptFeedbacks.stream()
                .filter(f -> f.getStatus() == Feedback.FeedbackStatus.COMPLETED)
                .count();
            
            // Calculate overdue: feedbacks not completed and older than 7 days
            long overdue = deptFeedbacks.stream()
                .filter(f -> f.getStatus() != Feedback.FeedbackStatus.COMPLETED 
                    && f.getReceivedDate() != null 
                    && f.getReceivedDate().isBefore(today.minusDays(OVERDUE_DAYS)))
                .count();
            
            // Calculate avgDays: average processing days for completed feedbacks
            double avgDays = deptFeedbacks.stream()
                .filter(f -> f.getStatus() == Feedback.FeedbackStatus.COMPLETED 
                    && f.getCompletedDate() != null 
                    && f.getReceivedDate() != null)
                .mapToLong(f -> {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(f.getReceivedDate(), f.getCompletedDate());
                    return days;
                })
                .average()
                .orElse(0.0);
            
            // Calculate completionRate: (completed / total) * 100
            double completionRate = total > 0 ? (completed * 100.0 / total) : 0.0;
            
            deptData.put("total", total);
            deptData.put("pending", pending);
            deptData.put("processing", processing);
            deptData.put("completed", completed);
            deptData.put("overdue", overdue);
            deptData.put("avgDays", Math.round(avgDays * 10.0) / 10.0); // Round to 1 decimal
            deptData.put("completionRate", Math.round(completionRate));
            
            return deptData;
        }).collect(Collectors.toList());
    }
    
    public List<Map<String, Object>> getByDoctor(LocalDate dateFrom, LocalDate dateTo, Long departmentId) {
        List<Feedback> allFeedbacks = feedbackRepository.findAll();
        
        List<Feedback> filteredFeedbacks = allFeedbacks.stream()
            .filter(f -> dateFrom == null || f.getReceivedDate() == null || !f.getReceivedDate().isBefore(dateFrom))
            .filter(f -> dateTo == null || f.getReceivedDate() == null || !f.getReceivedDate().isAfter(dateTo))
            .filter(f -> departmentId == null || f.getDepartmentId() == null || f.getDepartmentId().equals(departmentId))
            .collect(Collectors.toList());
        
        Map<Long, List<Feedback>> byDoctor = filteredFeedbacks.stream()
            .filter(f -> f.getDoctorId() != null)
            .collect(Collectors.groupingBy(Feedback::getDoctorId));
        
        return byDoctor.entrySet().stream().map(e -> {
            Long doctorId = e.getKey();
            List<Feedback> doctorFeedbacks = e.getValue();
            
            Map<String, Object> doctorData = new HashMap<>();
            doctorData.put("id", doctorId);
            
            // Get doctor info from repository
            final String[] doctorName = {"Doctor " + doctorId};
            final String[] specialty = {""};
            final String[] deptName = {""};
            doctorRepository.findById(doctorId).ifPresent(doctor -> {
                doctorName[0] = doctor.getFullName();
                specialty[0] = doctor.getSpecialty() != null ? doctor.getSpecialty() : "";
                if (doctor.getDepartmentId() != null) {
                    departmentRepository.findById(doctor.getDepartmentId())
                        .ifPresent(dept -> deptName[0] = dept.getName());
                }
            });
            
            doctorData.put("doctorName", doctorName[0]);
            doctorData.put("specialty", specialty[0]);
            doctorData.put("departmentName", deptName[0]);
            
            long total = doctorFeedbacks.size();
            long completed = doctorFeedbacks.stream()
                .filter(f -> f.getStatus() == Feedback.FeedbackStatus.COMPLETED)
                .count();
            
            // Calculate avgDays: average processing days for completed feedbacks
            double avgDays = doctorFeedbacks.stream()
                .filter(f -> f.getStatus() == Feedback.FeedbackStatus.COMPLETED 
                    && f.getCompletedDate() != null 
                    && f.getReceivedDate() != null)
                .mapToLong(f -> {
                    long days = java.time.temporal.ChronoUnit.DAYS.between(f.getReceivedDate(), f.getCompletedDate());
                    return days;
                })
                .average()
                .orElse(0.0);
            
            // Get average rating from RatingRepository
            Double avgRating = ratingRepository.getAverageRatingByDoctorId(doctorId);
            double rating = avgRating != null ? avgRating : 0.0;
            
            doctorData.put("total", total);
            doctorData.put("completed", completed);
            doctorData.put("avgDays", Math.round(avgDays * 10.0) / 10.0); // Round to 1 decimal
            doctorData.put("rating", Math.round(rating * 10.0) / 10.0); // Round to 1 decimal
            
            return doctorData;
        }).collect(Collectors.toList());
    }
    
    public List<FeedbackDTO> getFeedbacksWithImages(LocalDate dateFrom, LocalDate dateTo, Long departmentId) {
        // Get all feedback IDs that have images
        List<FeedbackImage> allImages = feedbackImageRepository.findAll();
        List<Long> feedbackIdsWithImages = allImages.stream()
            .map(FeedbackImage::getFeedbackId)
            .distinct()
            .collect(Collectors.toList());
        
        if (feedbackIdsWithImages.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Get feedbacks by IDs
        List<Feedback> feedbacksWithImages = feedbackRepository.findAllById(feedbackIdsWithImages);
        
        // Filter by date range and department
        List<Feedback> filteredFeedbacks = feedbacksWithImages.stream()
            .filter(f -> dateFrom == null || f.getReceivedDate() == null || !f.getReceivedDate().isBefore(dateFrom))
            .filter(f -> dateTo == null || f.getReceivedDate() == null || !f.getReceivedDate().isAfter(dateTo))
            .filter(f -> departmentId == null || f.getDepartmentId() == null || f.getDepartmentId().equals(departmentId))
            .sorted((f1, f2) -> f2.getCode().compareTo(f1.getCode())) // Sort by code DESC
            .collect(Collectors.toList());
        
        // Convert to DTOs with images
        return filteredFeedbacks.stream().map(f -> {
            FeedbackDTO dto = new FeedbackDTO();
            dto.setId(f.getId());
            dto.setCode(f.getCode());
            dto.setContent(f.getContent());
            dto.setChannel(f.getChannel());
            dto.setLevel(f.getLevel());
            dto.setStatus(f.getStatus());
            dto.setReceivedDate(f.getReceivedDate());
            dto.setDepartmentId(f.getDepartmentId());
            dto.setDoctorId(f.getDoctorId());
            dto.setHandlerId(f.getHandlerId());
            dto.setReceiverId(f.getReceiverId());
            
            // Map department name
            if (f.getDepartmentId() != null) {
                departmentRepository.findById(f.getDepartmentId())
                    .ifPresent(dept -> dto.setDepartmentName(dept.getName()));
            }
            
            // Map doctor name
            if (f.getDoctorId() != null) {
                doctorRepository.findById(f.getDoctorId())
                    .ifPresent(doctor -> dto.setDoctorName(doctor.getFullName()));
            }
            
            // Map handler name
            if (f.getHandlerId() != null) {
                userRepository.findById(f.getHandlerId())
                    .ifPresent(handler -> dto.setHandlerName(handler.getFullName()));
            }
            
            // Map receiver name
            if (f.getReceiverId() != null) {
                userRepository.findById(f.getReceiverId())
                    .ifPresent(receiver -> dto.setReceiverName(receiver.getFullName()));
            }
            
            // Load images
            List<FeedbackImage> images = feedbackImageRepository.findByFeedbackId(f.getId());
            dto.setImages(images.stream().map(img -> {
                FeedbackImageDTO imgDTO = new FeedbackImageDTO();
                imgDTO.setId(img.getId());
                imgDTO.setFilename(img.getFilename());
                imgDTO.setUrl("/api/upload/images/" + img.getFilename());
                imgDTO.setImageType(img.getImageType().name());
                return imgDTO;
            }).collect(Collectors.toList()));
            
            return dto;
        }).collect(Collectors.toList());
    }
    
    public byte[] exportExcel(LocalDate dateFrom, LocalDate dateTo, String type, Long departmentId) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet;
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle percentageStyle = createPercentageStyle(workbook);
            
            if ("by-doctor".equals(type)) {
                sheet = workbook.createSheet("Báo cáo theo bác sĩ");
                List<Map<String, Object>> data = getByDoctor(dateFrom, dateTo, departmentId);
                writeDoctorReport(sheet, data, headerStyle, dataStyle, numberStyle);
            } else if ("by-department".equals(type)) {
                sheet = workbook.createSheet("Báo cáo theo phòng ban");
                List<Map<String, Object>> data = getByDepartment(dateFrom, dateTo, departmentId);
                writeDepartmentReport(sheet, data, headerStyle, dataStyle, numberStyle, percentageStyle);
            } else {
                throw new IllegalArgumentException("Invalid export type: " + type);
            }
            
            // Auto-size columns
            for (int i = 0; i < sheet.getRow(0).getLastCellNum(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
    
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.0"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
    
    private CellStyle createPercentageStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0%"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }
    
    private void writeDoctorReport(Sheet sheet, List<Map<String, Object>> data, 
                                   CellStyle headerStyle, CellStyle dataStyle, CellStyle numberStyle) {
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Tên bác sĩ", "Chuyên khoa", "Phòng ban", "Tổng số phản ánh", 
                           "Đã hoàn thành", "Số ngày trung bình", "Đánh giá"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Write data rows
        int rowNum = 1;
        for (Map<String, Object> rowData : data) {
            Row row = sheet.createRow(rowNum++);
            
            int colNum = 0;
            // Tên bác sĩ
            createCell(row, colNum++, getStringValue(rowData.get("doctorName")), dataStyle);
            // Chuyên khoa
            createCell(row, colNum++, getStringValue(rowData.get("specialty")), dataStyle);
            // Phòng ban
            createCell(row, colNum++, getStringValue(rowData.get("departmentName")), dataStyle);
            // Tổng số phản ánh
            createCell(row, colNum++, getLongValue(rowData.get("total")), dataStyle);
            // Đã hoàn thành
            createCell(row, colNum++, getLongValue(rowData.get("completed")), dataStyle);
            // Số ngày trung bình
            Cell avgDaysCell = row.createCell(colNum++);
            Object avgDaysObj = rowData.get("avgDays");
            if (avgDaysObj != null) {
                avgDaysCell.setCellValue(getDoubleValue(avgDaysObj));
                avgDaysCell.setCellStyle(numberStyle);
            } else {
                avgDaysCell.setCellValue(0.0);
                avgDaysCell.setCellStyle(numberStyle);
            }
            // Đánh giá
            Cell ratingCell = row.createCell(colNum++);
            Object ratingObj = rowData.get("rating");
            if (ratingObj != null) {
                ratingCell.setCellValue(getDoubleValue(ratingObj));
                ratingCell.setCellStyle(numberStyle);
            } else {
                ratingCell.setCellValue(0.0);
                ratingCell.setCellStyle(numberStyle);
            }
        }
    }
    
    private void writeDepartmentReport(Sheet sheet, List<Map<String, Object>> data,
                                      CellStyle headerStyle, CellStyle dataStyle, 
                                      CellStyle numberStyle, CellStyle percentageStyle) {
        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Tên phòng ban", "Tổng số", "Chờ xử lý", "Đang xử lý", 
                           "Đã hoàn thành", "Quá hạn", "Số ngày trung bình", "Tỷ lệ hoàn thành (%)"};
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Write data rows
        int rowNum = 1;
        for (Map<String, Object> rowData : data) {
            Row row = sheet.createRow(rowNum++);
            
            int colNum = 0;
            // Tên phòng ban
            createCell(row, colNum++, getStringValue(rowData.get("departmentName")), dataStyle);
            // Tổng số
            createCell(row, colNum++, getLongValue(rowData.get("total")), dataStyle);
            // Chờ xử lý
            createCell(row, colNum++, getLongValue(rowData.get("pending")), dataStyle);
            // Đang xử lý
            createCell(row, colNum++, getLongValue(rowData.get("processing")), dataStyle);
            // Đã hoàn thành
            createCell(row, colNum++, getLongValue(rowData.get("completed")), dataStyle);
            // Quá hạn
            createCell(row, colNum++, getLongValue(rowData.get("overdue")), dataStyle);
            // Số ngày trung bình
            Cell avgDaysCell = row.createCell(colNum++);
            Object avgDaysObj = rowData.get("avgDays");
            if (avgDaysObj != null) {
                avgDaysCell.setCellValue(getDoubleValue(avgDaysObj));
                avgDaysCell.setCellStyle(numberStyle);
            } else {
                avgDaysCell.setCellValue(0.0);
                avgDaysCell.setCellStyle(numberStyle);
            }
            // Tỷ lệ hoàn thành (%)
            Cell completionRateCell = row.createCell(colNum++);
            Object completionRateObj = rowData.get("completionRate");
            if (completionRateObj != null) {
                double rate = getDoubleValue(completionRateObj) / 100.0;
                completionRateCell.setCellValue(rate);
                completionRateCell.setCellStyle(percentageStyle);
            } else {
                completionRateCell.setCellValue(0.0);
                completionRateCell.setCellStyle(percentageStyle);
            }
        }
    }
    
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }
    
    private void createCell(Row row, int column, Long value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : 0L);
        cell.setCellStyle(style);
    }
    
    private String getStringValue(Object obj) {
        return obj != null ? obj.toString() : "";
    }
    
    private Long getLongValue(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
    
    private Double getDoubleValue(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

