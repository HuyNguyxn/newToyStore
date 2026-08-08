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
                "Thanh toán thành công",
                "Thanh toán cho đơn hàng #" + event.orderId() + " đã hoàn tất thành công.",
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
                "Thanh toán thất bại",
                "Thanh toán cho đơn hàng #" + event.orderId() + " thất bại. Lý do: " + event.reason(),
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
                "Hoàn tiền thành công",
                "Yêu cầu hoàn tiền cho đơn hàng #" + event.orderId() + " đã được xử lý.",
                "PAYMENT_REFUNDED:" + event.refundId(),
                event.occurredAt(),
                true
        );
    }
}
