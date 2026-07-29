package com.example.new_toy_store.notification.application.listener;

import com.example.new_toy_store.global.event.OrderStatusChangedEvent;
import com.example.new_toy_store.notification.application.NotificationFacade;
import com.example.new_toy_store.notification.domain.NotificationReferenceType;
import com.example.new_toy_store.notification.domain.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class OrderNotificationEventListener {

    private final NotificationFacade notificationFacade;

    public OrderNotificationEventListener(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        NotificationType type = "CANCELLED".equals(event.currentStatus().name())
                ? NotificationType.ORDER_CANCELLED
                : NotificationType.ORDER_STATUS_CHANGED;
        notificationFacade.notifyUser(
                event.userId(),
                type,
                NotificationReferenceType.ORDER,
                event.orderId(),
                "Order #" + event.orderId() + " status updated",
                "Your order moved from " + event.previousStatus().name() + " to " + event.currentStatus().name() + ".",
                "ORDER_STATUS:" + event.orderId() + ":" + event.currentStatus().name(),
                event.occurredAt(),
                true
        );
    }
}
