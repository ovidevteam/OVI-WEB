package ovi.web.dhybe.specification;

import org.springframework.data.jpa.domain.Specification;
import ovi.web.dhybe.dto.feedback.FeedbackFilterCriteria;
import ovi.web.dhybe.entity.Feedback;

import java.time.LocalDateTime;

public final class FeedbackSpecifications {

    private FeedbackSpecifications() {
    }

    public static Specification<Feedback> withFilters(FeedbackFilterCriteria criteria) {
        return Specification.where(byDepartment(criteria.getDepartmentId()))
                .and(byDoctor(criteria.getDoctorId()))
                .and(byStatus(criteria.getStatus()))
                .and(byLevel(criteria.getLevel()))
                .and(byHandler(criteria.getHandlerId()))
                .and(byReceiver(criteria.getReceiverId()))
                .and(byDateRange(criteria.getFromDate(), criteria.getToDate()))
                .and(byKeyword(criteria.getKeyword()))
                .and(ratedOnly(criteria.getRatedOnly()));
    }

    private static Specification<Feedback> byDepartment(Long departmentId) {
        return (root, query, cb) -> departmentId == null
                ? null
                : cb.equal(root.get("department").get("id"), departmentId);
    }

    private static Specification<Feedback> byDoctor(Long doctorId) {
        return (root, query, cb) -> doctorId == null
                ? null
                : cb.equal(root.get("doctor").get("id"), doctorId);
    }

    private static Specification<Feedback> byStatus(Enum<?> status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<Feedback> byLevel(Enum<?> level) {
        return (root, query, cb) -> level == null ? null : cb.equal(root.get("level"), level);
    }

    private static Specification<Feedback> byHandler(Long handlerId) {
        return (root, query, cb) -> handlerId == null
                ? null
                : cb.equal(root.get("handler").get("id"), handlerId);
    }

    private static Specification<Feedback> byReceiver(Long receiverId) {
        return (root, query, cb) -> receiverId == null
                ? null
                : cb.equal(root.get("receiver").get("id"), receiverId);
    }

    private static Specification<Feedback> byDateRange(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) {
                return null;
            }
            if (from != null && to != null) {
                return cb.between(root.get("receivedDate"), from, to);
            }
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("receivedDate"), from);
            }
            return cb.lessThanOrEqualTo(root.get("receivedDate"), to);
        };
    }

    private static Specification<Feedback> byKeyword(String keyword) {
        return (root, query, cb) -> keyword == null || keyword.isBlank()
                ? null
                : cb.like(cb.lower(root.get("content")), "%" + keyword.toLowerCase() + "%");
    }

    private static Specification<Feedback> ratedOnly(Boolean ratedOnly) {
        return (root, query, cb) -> {
            if (ratedOnly == null) {
                return null;
            }
            if (ratedOnly) {
                return cb.isNotNull(root.get("rating"));
            }
            return cb.isNull(root.get("rating"));
        };
    }
}





