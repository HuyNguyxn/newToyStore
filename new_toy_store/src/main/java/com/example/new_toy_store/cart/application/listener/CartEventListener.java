package com.example.new_toy_store.cart.application.listener;

import com.example.new_toy_store.cart.application.service.CartService;
import com.example.new_toy_store.global.event.OrderCreatedEvent;
import com.example.new_toy_store.global.event.OrderCreationFailedEvent;
import com.example.new_toy_store.global.event.ProductUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CartEventListener {

    private static final Logger log = LoggerFactory.getLogger(CartEventListener.class);
    private final CartService cartService;

    public CartEventListener(CartService cartService) {
        this.cartService = cartService;
    }

    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.info("Order {} created successfully. Clearing cart {}", event.getOrderId(), event.getCartId());
        cartService.clearCheckedOutItems(event.getCartId());
    }

    @EventListener
    public void handleOrderFailed(OrderCreationFailedEvent event) {
        log.warn("Order creation failed for cart {}. Reason: {}. Unlocking cart.", event.getCartId(), event.getReason());
        cartService.unlockCart(event.getCartId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductUpdated(ProductUpdatedEvent event) {
        log.info("Product Variant {} price updated. Syncing carts...", event.getVariantId());
        cartService.syncProductChanges(event.getVariantId(), event.getNewPrice());
    }
}
