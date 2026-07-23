package com.example.new_toy_store.cart.application.listener;

import com.example.new_toy_store.global.event.CartStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CartStatusEventListener {

    private static final Logger log = LoggerFactory.getLogger(CartStatusEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleStatusChanged(CartStatusChangedEvent event) {
        log.info(
                "Cart {} for user {} changed status from {} to {} at {}",
                event.cartId(),
                event.userId(),
                event.previousStatus().getCode(),
                event.currentStatus().getCode(),
                event.occurredAt()
        );
    }
}
