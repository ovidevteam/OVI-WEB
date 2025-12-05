package ovi.web.dhybe.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ovi.web.dhybe.dto.auth.UserDto;
import ovi.web.dhybe.dto.feedback.*;
import ovi.web.dhybe.entity.*;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FeedbackMapper {

    @Value("${app.storage.base-dir:uploads}")
    private String baseDir;

    @Value("${app.storage.public-base-url:/uploads}")
    private String publicBaseUrl;

    public FeedbackListItemResponse toListItem(Feedback feedback) {
        UserDto handler = feedback.getHandler();
        Doctor doctor = feedback.getDoctor();
        Department department = feedback.getDepartment();
        return FeedbackListItemResponse.builder()
                .id(feedback.getId())
                .code(feedback.getCode())
                .status(feedback.getStatus())
                .level(feedback.getLevel())
                .departmentName(department != null ? department.getName() : null)
                .doctorName(doctor != null ? doctor.getFullName() : null)
                .receivedDate(feedback.getReceivedDate())
                .completedDate(feedback.getCompletedDate())
                .handlerId(handler != null ? (long) handler.getId() : null)
                .handlerName(handler != null ? handler.getFullName() : null)
                .build();
    }

    public FeedbackDetailResponse toDetail(Feedback feedback,
                                           List<FeedbackImage> images,
                                           List<FeedbackLog> logs) {
        List<FeedbackImageResponse> imageResponses = images.stream()
                .map(this::toImageResponse)
                .collect(Collectors.toList());
        List<FeedbackHistoryItem> historyItems = logs.stream()
                .map(this::toHistoryItem)
                .collect(Collectors.toList());

        return FeedbackDetailResponse.builder()
                .id(feedback.getId())
                .code(feedback.getCode())
                .status(feedback.getStatus())
                .level(feedback.getLevel())
                .content(feedback.getContent())
                .channel(feedback.getChannel())
                .departmentId(feedback.getDepartment() != null ? feedback.getDepartment().getId() : null)
                .departmentName(feedback.getDepartment() != null ? feedback.getDepartment().getName() : null)
                .doctorId(feedback.getDoctor() != null ? feedback.getDoctor().getId() : null)
                .doctorName(feedback.getDoctor() != null ? feedback.getDoctor().getFullName() : null)
                .handlerId(feedback.getHandler() != null ? (long) feedback.getHandler().getId() : null)
                .handlerName(feedback.getHandler() != null ? feedback.getHandler().getFullName() : null)
                .receiverId(feedback.getReceiver() != null ? (long) feedback.getReceiver().getId() : null)
                .receiverName(feedback.getReceiver() != null ? feedback.getReceiver().getFullName() : null)
                .receivedDate(feedback.getReceivedDate())
                .completedDate(feedback.getCompletedDate())
                .processNote(feedback.getProcessNote())
                .processCount(feedback.getProcessCount())
                .assignedDate(feedback.getAssignedDate())
                .lastProcessDate(feedback.getLastProcessDate())
                .images(imageResponses)
                .history(historyItems)
                .logs(historyItems)
                .build();
    }

    public FeedbackImageResponse toImageResponse(FeedbackImage image) {
        UserDto uploader = image.getUploadedBy();
        String path = image.getImagePath();
        String relative = path;
        if (baseDir != null && !baseDir.isEmpty() && path.startsWith(baseDir)) {
            relative = path.substring(baseDir.length());
            if (relative.startsWith("/") || relative.startsWith("\\")) {
                relative = relative.substring(1);
            }
        }
        relative = relative.replace("\\", "/");
        String url = publicBaseUrl + "/" + relative;

        return FeedbackImageResponse.builder()
                .id(image.getId())
                .path(image.getImagePath())
                .url(url)
                .imageType(image.getImageType())
                .uploadedBy(uploader != null ? (long) uploader.getId() : null)
                .uploadedByName(uploader != null ? uploader.getFullName() : null)
                .uploadedDate(image.getUploadedDate())
                .build();
    }

    public FeedbackHistoryItem toHistoryItem(FeedbackLog log) {
        UserDto actor = log.getUser();
        return FeedbackHistoryItem.builder()
                .id(log.getId())
                .action(log.getAction())
                .oldStatus(log.getOldStatus())
                .newStatus(log.getNewStatus())
                .note(log.getNote())
                .userId(actor != null ? (long) actor.getId() : null)
                .userName(actor != null ? actor.getFullName() : null)
                .createdDate(log.getCreatedDate())
                .build();
    }
}

