package com.example.new_toy_store.notification.application;

import com.example.new_toy_store.notification.application.dto.request.BroadcastNotificationRequest;
import com.example.new_toy_store.notification.application.dto.request.NotificationFilterRequest;
import com.example.new_toy_store.notification.application.dto.request.NotificationPreferenceRequest;
import com.example.new_toy_store.notification.application.dto.response.BroadcastNotificationResponse;
import com.example.new_toy_store.notification.application.dto.response.NotificationPreferenceResponse;
import com.example.new_toy_store.notification.application.dto.response.NotificationResponse;
import com.example.new_toy_store.notification.application.dto.response.UnreadNotificationCountResponse;
import com.example.new_toy_store.notification.domain.NotificationReferenceType;
import com.example.new_toy_store.notification.domain.NotificationType;
import com.example.new_toy_store.user.application.UserFacade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class NotificationFacade {

    private final NotificationService notificationService;
    private final UserFacade userFacade;

    public NotificationFacade(NotificationService notificationService, UserFacade userFacade) {
        this.notificationService = notificationService;
        this.userFacade = userFacade;
    }

    public void notifyUser(
            Integer userId,
            NotificationType type,
            NotificationReferenceType referenceType,
            Integer referenceId,
            String title,
            String message,
            String deduplicationKey,
            Instant occurredAt,
            boolean requestEmail
    ) {
        notificationService.createFromEvent(
                userId,
                type,
                referenceType,
                referenceId,
                title,
                message,
                deduplicationKey,
                occurredAt,
                requestEmail
        );
    }

    public Page<NotificationResponse> filterCurrentUser(String email, NotificationFilterRequest request, Pageable pageable) {
        return notificationService.filter(userFacade.getAuthenticatedUserId(email), request, pageable);
    }

    public NotificationResponse getCurrentUserDetail(String email, Integer notificationId) {
        return notificationService.getDetail(userFacade.getAuthenticatedUserId(email), notificationId);
    }

    public UnreadNotificationCountResponse countCurrentUserUnread(String email) {
        return notificationService.countUnread(userFacade.getAuthenticatedUserId(email));
    }

    public NotificationResponse markCurrentUserAsRead(String email, Integer notificationId) {
        return notificationService.markAsRead(userFacade.getAuthenticatedUserId(email), notificationId);
    }

    public NotificationResponse archiveCurrentUser(String email, Integer notificationId) {
        return notificationService.archive(userFacade.getAuthenticatedUserId(email), notificationId);
    }

    public int markAllCurrentUserAsRead(String email) {
        return notificationService.markAllAsRead(userFacade.getAuthenticatedUserId(email));
    }

    public NotificationPreferenceResponse getCurrentUserPreferences(String email) {
        return notificationService.getPreferences(userFacade.getAuthenticatedUserId(email));
    }

    public NotificationPreferenceResponse updateCurrentUserPreferences(String email, NotificationPreferenceRequest request) {
        return notificationService.updatePreferences(userFacade.getAuthenticatedUserId(email), request);
    }

    public BroadcastNotificationResponse broadcast(BroadcastNotificationRequest request) {
        return notificationService.broadcast(request, userFacade.getActiveNotificationRecipients());
    }
}
