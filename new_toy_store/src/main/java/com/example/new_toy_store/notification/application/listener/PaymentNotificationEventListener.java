package com.example.new_toy_store.notification.application.listener;

import com.example.new_toy_store.global.event.PaymentCompletedEvent;
import com.example.new_toy_store.global.event.PaymentFailedEvent;
import com.example.new_toy_store.global.event.PaymentRefundedEvent;
import com.example.new_toy_store.notification.application.NotificationFacade;
import com.example.new_toy_store.notification.domain.NotificationReferenceType;
import com.example.new_toy_store.notification.domain.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PaymentNotificationEventListener {

    private final NotificationFacade notificationFacade;

    public PaymentNotificationEventListener(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        notificationFacade.notifyUser(
                event.userId(),
                NotificationType.PAYMENT_COMPLETED,
                NotificationReferenceType.PAYMENT,
                event.paymentId(),
                "Payment completed",
                "Payment for order #" + event.orderId() + " was completed successfully.",
                "PAYMENT_COMPLETED:" + event.paymentId(),
                event.occurredAt(),
                true
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handlePaymentFailed(PaymentFailedEvent event) {
        notificationFacade.notifyUser(
                event.userId(),
                NotificationType.PAYMENT_FAILED,
                NotificationReferenceType.PAYMENT,
                event.paymentId(),
                "Payment failed",
                "Payment for order #" + event.orderId() + " failed. Reason: " + event.reason(),
                "PAYMENT_FAILED:" + event.paymentId(),
                event.occurredAt(),
                true
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handlePaymentRefunded(PaymentRefundedEvent event) {
        notificationFacade.notifyUser(
                event.userId(),
                NotificationType.PAYMENT_REFUNDED,
                NotificationReferenceType.PAYMENT,
                event.paymentId(),
                "Refund completed",
                "Refund for order #" + event.orderId() + " was completed.",
                "PAYMENT_REFUNDED:" + event.refundId(),
                event.occurredAt(),
                true
        );
    }
}
