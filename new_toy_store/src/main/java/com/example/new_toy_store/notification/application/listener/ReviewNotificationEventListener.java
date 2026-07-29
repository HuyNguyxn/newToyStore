package com.example.new_toy_store.notification.application.listener;

import com.example.new_toy_store.global.event.ReviewRepliedEvent;
import com.example.new_toy_store.global.event.ReviewStatusChangedEvent;
import com.example.new_toy_store.notification.application.NotificationFacade;
import com.example.new_toy_store.notification.domain.NotificationReferenceType;
import com.example.new_toy_store.notification.domain.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ReviewNotificationEventListener {

    private final NotificationFacade notificationFacade;

    public ReviewNotificationEventListener(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleReviewReplied(ReviewRepliedEvent event) {
        notificationFacade.notifyUser(
                event.userId(),
                NotificationType.REVIEW_REPLIED,
                NotificationReferenceType.REVIEW,
                event.reviewId(),
                "Your review has a reply",
                "A staff member replied to your product review.",
                "REVIEW_REPLIED:" + event.reviewId(),
                event.occurredAt(),
                false
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleReviewStatusChanged(ReviewStatusChangedEvent event) {
        notificationFacade.notifyUser(
                event.userId(),
                NotificationType.REVIEW_STATUS_CHANGED,
                NotificationReferenceType.REVIEW,
                event.reviewId(),
                "Review status updated",
                "Your review status changed to " + event.currentStatus().name() + ".",
                "REVIEW_STATUS:" + event.reviewId() + ":" + event.currentStatus().name(),
                event.occurredAt(),
                false
        );
    }
}
