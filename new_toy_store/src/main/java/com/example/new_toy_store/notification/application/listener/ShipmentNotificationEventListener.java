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
                NotificationType.SHIPMENT_CREATED, "Shipment created", "Shipment for order #" + event.orderId() + " was created.", event.occurredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleShipmentInTransit(ShipmentInTransitEvent event) {
        notifyShipment(event.userId(), event.shipmentId(), event.orderId(), event.trackingCode(),
                NotificationType.SHIPMENT_IN_TRANSIT, "Shipment in transit", "Order #" + event.orderId() + " is now in transit.", event.occurredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleShipmentDelivered(ShipmentDeliveredEvent event) {
        notifyShipment(event.userId(), event.shipmentId(), event.orderId(), event.trackingCode(),
                NotificationType.SHIPMENT_DELIVERED, "Shipment delivered", "Order #" + event.orderId() + " was delivered.", event.occurredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleShipmentReturned(ShipmentReturnedEvent event) {
        notifyShipment(event.userId(), event.shipmentId(), event.orderId(), event.trackingCode(),
                NotificationType.SHIPMENT_RETURNED, "Shipment returned", "Shipment for order #" + event.orderId() + " was returned.", event.occurredAt());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleShipmentCancelled(ShipmentCancelledEvent event) {
        notifyShipment(event.userId(), event.shipmentId(), event.orderId(), event.trackingCode(),
                NotificationType.SHIPMENT_CANCELLED, "Shipment cancelled", "Shipment for order #" + event.orderId() + " was cancelled.", event.occurredAt());
    }

    private void notifyShipment(Integer userId, Integer shipmentId, Integer orderId, String trackingCode,
                                NotificationType type, String title, String message, java.time.Instant occurredAt) {
        notificationFacade.notifyUser(
                userId,
                type,
                NotificationReferenceType.SHIPMENT,
                shipmentId,
                title,
                message + " Tracking code: " + trackingCode,
                type.name() + ":" + shipmentId,
                occurredAt,
                true
        );
    }
}
