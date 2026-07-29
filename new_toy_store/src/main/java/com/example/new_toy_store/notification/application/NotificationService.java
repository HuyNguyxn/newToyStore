package com.example.new_toy_store.notification.application;

import com.example.new_toy_store.global.event.NotificationEmailRequestedEvent;
import com.example.new_toy_store.infrastructure.specification.NotificationSpecification;
import com.example.new_toy_store.notification.application.dto.request.BroadcastNotificationRequest;
import com.example.new_toy_store.notification.application.dto.request.NotificationFilterRequest;
import com.example.new_toy_store.notification.application.dto.request.NotificationPreferenceRequest;
import com.example.new_toy_store.notification.application.dto.response.BroadcastNotificationResponse;
import com.example.new_toy_store.notification.application.dto.response.NotificationPreferenceResponse;
import com.example.new_toy_store.notification.application.dto.response.NotificationResponse;
import com.example.new_toy_store.notification.application.dto.response.UnreadNotificationCountResponse;
import com.example.new_toy_store.notification.domain.Notification;
import com.example.new_toy_store.notification.domain.NotificationPreference;
import com.example.new_toy_store.notification.domain.NotificationPreferenceRepository;
import com.example.new_toy_store.notification.domain.NotificationReferenceType;
import com.example.new_toy_store.notification.domain.NotificationRepository;
import com.example.new_toy_store.notification.domain.NotificationStatus;
import com.example.new_toy_store.notification.domain.NotificationType;
import com.example.new_toy_store.notification.domain.exception.NotificationAccessDeniedException;
import com.example.new_toy_store.notification.domain.exception.NotificationNotFoundException;
import com.example.new_toy_store.notification.mapper.NotificationMapper;
import com.example.new_toy_store.user.application.dto.response.NotificationRecipientResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ZoneId zoneId;
    private final int retentionDays;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationPreferenceRepository preferenceRepository,
            ApplicationEventPublisher eventPublisher,
            @Value("${app.notification.zone:Asia/Ho_Chi_Minh}") String zoneId,
            @Value("${app.notification.retention-days:90}") int retentionDays
    ) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.eventPublisher = eventPublisher;
        this.zoneId = ZoneId.of(zoneId);
        this.retentionDays = retentionDays;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createFromEvent(
            Integer recipientUserId,
            NotificationType type,
            NotificationReferenceType referenceType,
            Integer referenceId,
            String title,
            String message,
            String deduplicationKey,
            Instant occurredAt,
            boolean requestEmail
    ) {
        NotificationPreference preference = getPreferenceOrDefault(recipientUserId);
        boolean inAppAllowed = preference.allowsInApp(type);
        boolean emailAllowed = requestEmail && preference.allowsEmail(type);

        if (!inAppAllowed && !emailAllowed) {
            return;
        }
        if (deduplicationKey != null && notificationRepository.existsByDeduplicationKey(deduplicationKey)) {
            return;
        }

        Notification notification = null;
        if (inAppAllowed) {
            notification = notificationRepository.save(new Notification(
                    recipientUserId,
                    type,
                    referenceType,
                    referenceId,
                    title,
                    message,
                    referenceType.buildActionUrl(referenceId),
                    deduplicationKey,
                    toLocalDateTime(occurredAt),
                    LocalDateTime.now(zoneId).plusDays(retentionDays)
            ));
        }

        if (emailAllowed) {
            eventPublisher.publishEvent(NotificationEmailRequestedEvent.now(
                    notification == null ? null : notification.getId(),
                    recipientUserId,
                    type,
                    title,
                    message
            ));
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> filter(Integer userId, NotificationFilterRequest request, Pageable pageable) {
        return notificationRepository.findAll(NotificationSpecification.filter(userId, request), pageable)
                .map(NotificationMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public NotificationResponse getDetail(Integer userId, Integer notificationId) {
        return NotificationMapper.toResponse(getOwnedNotification(userId, notificationId));
    }

    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse countUnread(Integer userId) {
        long count = notificationRepository.countUnreadNotExpired(userId, NotificationStatus.UNREAD, LocalDateTime.now(zoneId));
        return new UnreadNotificationCountResponse(count);
    }

    @Transactional
    public NotificationResponse markAsRead(Integer userId, Integer notificationId) {
        Notification notification = getOwnedNotification(userId, notificationId);
        notification.markAsRead();
        return NotificationMapper.toResponse(notification);
    }

    @Transactional
    public NotificationResponse archive(Integer userId, Integer notificationId) {
        Notification notification = getOwnedNotification(userId, notificationId);
        notification.archive();
        return NotificationMapper.toResponse(notification);
    }

    @Transactional
    public int markAllAsRead(Integer userId) {
        return notificationRepository.markAllAsRead(
                userId,
                NotificationStatus.UNREAD,
                NotificationStatus.READ,
                LocalDateTime.now(zoneId)
        );
    }

    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getPreferences(Integer userId) {
        return NotificationMapper.toResponse(getPreferenceOrDefault(userId));
    }

    @Transactional
    public NotificationPreferenceResponse updatePreferences(Integer userId, NotificationPreferenceRequest request) {
        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> new NotificationPreference(userId));
        preference.update(
                request.getInAppEnabled(),
                request.getEmailEnabled(),
                request.getOrderEnabled(),
                request.getPaymentEnabled(),
                request.getShipmentEnabled(),
                request.getReturnEnabled(),
                request.getReviewEnabled(),
                request.getCartEnabled(),
                request.getSystemEnabled()
        );
        return NotificationMapper.toResponse(preferenceRepository.save(preference));
    }

    @Transactional
    public BroadcastNotificationResponse broadcast(
            BroadcastNotificationRequest request,
            List<NotificationRecipientResponse> recipients
    ) {
        int created = 0;
        int skipped = 0;
        for (NotificationRecipientResponse recipient : recipients) {
            String dedupKey = "SYSTEM:" + request.getRequestKey().trim() + ":" + recipient.userId();
            if (notificationRepository.existsByDeduplicationKey(dedupKey)) {
                skipped++;
                continue;
            }
            createFromEvent(
                    recipient.userId(),
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    NotificationReferenceType.SYSTEM,
                    null,
                    request.getTitle(),
                    request.getMessage(),
                    dedupKey,
                    Instant.now(),
                    request.isSendEmail()
            );
            created++;
        }
        return new BroadcastNotificationResponse(created, skipped);
    }

    @Transactional
    public int cleanupExpired() {
        return notificationRepository.softDeleteExpired(LocalDateTime.now(zoneId));
    }

    private Notification getOwnedNotification(Integer userId, Integer notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationNotFoundException(notificationId));
        if (!notification.belongsTo(userId)) {
            throw new NotificationAccessDeniedException(notificationId, userId);
        }
        return notification;
    }

    private NotificationPreference getPreferenceOrDefault(Integer userId) {
        return preferenceRepository.findByUserId(userId)
                .orElseGet(() -> new NotificationPreference(userId));
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        Instant safeInstant = instant == null ? Instant.now() : instant;
        return LocalDateTime.ofInstant(safeInstant, zoneId);
    }
}
