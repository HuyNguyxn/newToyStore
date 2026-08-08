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
        boolean isCancelled = "CANCELLED".equals(event.currentStatus().name());
        NotificationType type = isCancelled ? NotificationType.ORDER_CANCELLED : NotificationType.ORDER_STATUS_CHANGED;
        
        String title = isCancelled 
                ? "Đơn hàng #" + event.orderId() + " đã bị hủy"
                : "Cập nhật đơn hàng #" + event.orderId();

        String statusVi = switch (event.currentStatus().name()) {
            case "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "SHIPPED" -> "Đang giao hàng";
            case "COMPLETED" -> "Đã hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            default -> event.currentStatus().name();
        };

        String message = "Đơn hàng #" + event.orderId() + " của bạn hiện đang ở trạng thái: " + statusVi + ".";

        notificationFacade.notifyUser(
                event.userId(),
                type,
                NotificationReferenceType.ORDER,
                event.orderId(),
                title,
                message,
                "ORDER_STATUS:" + event.orderId() + ":" + event.currentStatus().name(),
                event.occurredAt(),
                true
        );
    }
}
