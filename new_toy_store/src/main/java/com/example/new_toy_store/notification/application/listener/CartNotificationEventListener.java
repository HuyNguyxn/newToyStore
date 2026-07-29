package com.example.new_toy_store.notification.application.listener;

import com.example.new_toy_store.global.event.CartItemsExpiringEvent;
import com.example.new_toy_store.global.event.CartStatusChangedEvent;
import com.example.new_toy_store.notification.application.NotificationFacade;
import com.example.new_toy_store.notification.domain.NotificationReferenceType;
import com.example.new_toy_store.notification.domain.NotificationType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;

@Component
public class CartNotificationEventListener {

    private final NotificationFacade notificationFacade;

    public CartNotificationEventListener(NotificationFacade notificationFacade) {
        this.notificationFacade = notificationFacade;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleCartItemsExpiring(CartItemsExpiringEvent event) {
        for (CartItemsExpiringEvent.Item item : event.getItems()) {
            notificationFacade.notifyUser(
                    item.userId(),
                    NotificationType.CART_EXPIRING,
                    NotificationReferenceType.CART,
                    null,
                    "Cart item is expiring",
                    "A product in your cart will expire in " + event.getDaysLeft() + " day(s).",
                    "CART_EXPIRING:" + event.getDaysLeft() + ":" + item.userId() + ":" + item.productId() + ":" + item.variantId(),
                    Instant.now(),
                    false
            );
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleCartStatusChanged(CartStatusChangedEvent event) {
        notificationFacade.notifyUser(
                event.userId(),
                NotificationType.CART_EXPIRING,
                NotificationReferenceType.CART,
                event.cartId(),
                "Cart status updated",
                "Your cart status changed to " + event.currentStatus().name() + ".",
                "CART_STATUS:" + event.cartId() + ":" + event.currentStatus().name(),
                event.occurredAt(),
                false
        );
    }
}
