package com.example.new_toy_store.notification.domain;

import com.example.new_toy_store.global.common.BaseRootEntity;
import com.example.new_toy_store.notification.domain.converter.NotificationReferenceTypeConverter;
import com.example.new_toy_store.notification.domain.converter.NotificationStatusConverter;
import com.example.new_toy_store.notification.domain.converter.NotificationTypeConverter;
import com.example.new_toy_store.notification.domain.exception.InvalidNotificationOperationException;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notification_recipient_status_created", columnList = "recipient_user_id,status,created_at"),
                @Index(name = "idx_notification_recipient_type_created", columnList = "recipient_user_id,type,created_at"),
                @Index(name = "idx_notification_expires_at", columnList = "expires_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notification_deduplication_key", columnNames = "deduplication_key")
        }
)
public class Notification extends BaseRootEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "recipient_user_id", nullable = false)
    private Integer recipientUserId;

    @Convert(converter = NotificationTypeConverter.class)
    @Column(nullable = false, length = 40)
    private NotificationType type;

    @Convert(converter = NotificationStatusConverter.class)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.UNREAD;

    @Convert(converter = NotificationReferenceTypeConverter.class)
    @Column(name = "reference_type", nullable = false, length = 30)
    private NotificationReferenceType referenceType;

    @Column(name = "reference_id")
    private Integer referenceId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(name = "action_url", length = 255)
    private String actionUrl;

    @Column(name = "deduplication_key", nullable = false, length = 180)
    private String deduplicationKey;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    protected Notification() {
    }

    public Notification(
            Integer recipientUserId,
            NotificationType type,
            NotificationReferenceType referenceType,
            Integer referenceId,
            String title,
            String message,
            String actionUrl,
            String deduplicationKey,
            LocalDateTime occurredAt,
            LocalDateTime expiresAt
    ) {
        if (recipientUserId == null || recipientUserId <= 0) {
            throw InvalidNotificationOperationException.invalidData("recipientUserId", recipientUserId);
        }
        if (type == null || referenceType == null) {
            throw InvalidNotificationOperationException.invalidData("type", type);
        }
        if (title == null || title.isBlank() || message == null || message.isBlank()) {
            throw InvalidNotificationOperationException.invalidData("content", null);
        }
        if (deduplicationKey == null || deduplicationKey.isBlank()) {
            throw InvalidNotificationOperationException.invalidData("deduplicationKey", deduplicationKey);
        }
        this.recipientUserId = recipientUserId;
        this.type = type;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.title = title.trim();
        this.message = message.trim();
        this.actionUrl = actionUrl;
        this.deduplicationKey = deduplicationKey.trim();
        this.occurredAt = occurredAt == null ? LocalDateTime.now() : occurredAt;
        this.expiresAt = expiresAt;
    }

    public void markAsRead() {
        transitionTo(NotificationStatus.READ);
        this.readAt = LocalDateTime.now();
    }

    public void archive() {
        transitionTo(NotificationStatus.ARCHIVED);
    }

    private void transitionTo(NotificationStatus target) {
        if (status == target) return;
        if (!status.canTransitionTo(target)) {
            throw InvalidNotificationOperationException.invalidTransition(id, status, target);
        }
        this.status = target;
    }

    public boolean belongsTo(Integer userId) {
        return recipientUserId.equals(userId);
    }

    public Integer getId() { return id; }
    public Integer getRecipientUserId() { return recipientUserId; }
    public NotificationType getType() { return type; }
    public NotificationStatus getStatus() { return status; }
    public NotificationReferenceType getReferenceType() { return referenceType; }
    public Integer getReferenceId() { return referenceId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public String getActionUrl() { return actionUrl; }
    public String getDeduplicationKey() { return deduplicationKey; }
    public LocalDateTime getOccurredAt() { return occurredAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
