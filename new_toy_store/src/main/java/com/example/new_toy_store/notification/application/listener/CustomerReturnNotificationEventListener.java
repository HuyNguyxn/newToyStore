package com.example.new_toy_store.notification.application.listener;

import com.example.new_toy_store.global.event.CustomerReturnStatusChangedEvent;
import com.example.new_toy_store.notification.application.NotificationFacade;
import com.example.new_toy_store.notification.domain.NotificationReferenceType;
import com.example.new_toy_store.notification.domain.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CustomerReturnNotificationEventListener {

    private final NotificationFacade notificationFacade;

    public CustomerReturnNotificationEventListener(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleCustomerReturnStatusChanged(CustomerReturnStatusChangedEvent event) {
        var payload = event.payload();
        notificationFacade.notifyUser(
                payload.userId(),
                NotificationType.RETURN_STATUS_CHANGED,
                NotificationReferenceType.CUSTOMER_RETURN,
                payload.returnId(),
                "Return request status updated",
                "Your return request for order #" + payload.orderId() + " changed to " + payload.currentStatus().name() + ".",
                "RETURN_STATUS:" + payload.returnId() + ":" + payload.currentStatus().name(),
                event.occurredAt(),
                true
        );
    }
}
