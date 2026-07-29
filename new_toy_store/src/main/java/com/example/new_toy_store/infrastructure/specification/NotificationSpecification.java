package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.notification.application.dto.request.NotificationFilterRequest;
import com.example.new_toy_store.notification.domain.Notification;
import com.example.new_toy_store.notification.domain.NotificationStatus;
import com.example.new_toy_store.notification.domain.NotificationType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class NotificationSpecification {

    private NotificationSpecification() {
    }

    public static Specification<Notification> filter(Integer userId, NotificationFilterRequest request) {
        NotificationFilterRequest safeRequest = request == null ? new NotificationFilterRequest() : request;
        return Specification.where(belongsToUser(userId))
                .and(hasStatus(safeRequest.getStatus()))
                .and(hasType(safeRequest.getType()))
                .and(createdBetween(safeRequest.getFromDate(), safeRequest.getToDate()))
                .and(notExpired(LocalDateTime.now()));
    }

    public static Specification<Notification> belongsToUser(Integer userId) {
        return BaseSpecification.isEqual("recipientUserId", userId);
    }

    public static Specification<Notification> hasStatus(NotificationStatus status) {
        return BaseSpecification.isEqual("status", status);
    }

    public static Specification<Notification> hasType(NotificationType type) {
        return BaseSpecification.isEqual("type", type);
    }

    public static Specification<Notification> createdBetween(LocalDateTime from, LocalDateTime to) {
        return BaseSpecification.dateBetween("createdAt", from, to);
    }

    public static Specification<Notification> notExpired(LocalDateTime now) {
        return (root, query, cb) -> cb.or(
                cb.isNull(root.get("expiresAt")),
                cb.greaterThan(root.get("expiresAt"), now)
        );
    }
}
