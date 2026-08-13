package com.example.new_toy_store.notification.application.listener;

import com.example.new_toy_store.global.event.ShipmentCancelledEvent;
import com.example.new_toy_store.global.event.ShipmentCreatedEvent;
import com.example.new_toy_store.global.event.ShipmentDeliveredEvent;
import com.example.new_toy_store.global.event.ShipmentInTransitEvent;
import com.example.new_toy_store.global.event.ShipmentReturnedEvent;
import com.example.new_toy_store.notification.application.NotificationFacade;
import com.example.new_toy_store.notification.domain.NotificationReferenceType;
import com.example.new_toy_store.notification.domain.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ShipmentNotificationEventListener {

    private final NotificationFacade notificationFacade;

    public ShipmentNotificationEventListener(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleShipmentCreated(ShipmentCreatedEvent event) {
        notifyShipment(event.userId(), event.shipmentId(), event.orderId(), event.trackingCode(),
                NotificationType.SHIPMENT_CREATED, "Đã tạo đơn vận chuyển",
                shipmentMessage(event.orderId(), "đã được tạo"), event.occurredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleShipmentInTransit(ShipmentInTransitEvent event) {
        notifyShipment(event.userId(), event.shipmentId(), event.orderId(), event.trackingCode(),
                NotificationType.SHIPMENT_IN_TRANSIT, "Đang vận chuyển",
                shipmentMessage(event.orderId(), "đang được vận chuyển"), event.occurredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleShipmentDelivered(ShipmentDeliveredEvent event) {
        notifyShipment(event.userId(), event.shipmentId(), event.orderId(), event.trackingCode(),
                NotificationType.SHIPMENT_DELIVERED, "Giao hàng thành công",
                shipmentMessage(event.orderId(), "đã được giao thành công"), event.occurredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleShipmentReturned(ShipmentReturnedEvent event) {
        notifyShipment(event.userId(), event.shipmentId(), event.orderId(), event.trackingCode(),
                NotificationType.SHIPMENT_RETURNED, "Trả hàng thành công",
                shipmentMessage(event.orderId(), "đã được trả lại"), event.occurredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleShipmentCancelled(ShipmentCancelledEvent event) {
        notifyShipment(event.userId(), event.shipmentId(), event.orderId(), event.trackingCode(),
                NotificationType.SHIPMENT_CANCELLED, "Đã hủy đơn vận chuyển",
                shipmentMessage(event.orderId(), "đã bị hủy"), event.occurredAt());
    }

    private void notifyShipment(Integer userId, Integer shipmentId, Integer orderId, String trackingCode,
                                NotificationType type, String title, String message, java.time.Instant occurredAt) {
        notificationFacade.notifyUser(
                userId,
                type,
                NotificationReferenceType.SHIPMENT,
                shipmentId,
                title,
                message + (trackingCode != null && !trackingCode.isBlank() ? " Mã vận đơn: " + trackingCode : ""),
                type.name() + ":" + shipmentId,
                occurredAt,
                true
        );
    }

    private String shipmentMessage(Integer orderId, String action) {
        return orderId == null
                ? "Đơn vận chuyển " + action + "."
                : "Đơn vận chuyển cho đơn hàng #" + orderId + " " + action + ".";
    }
}
