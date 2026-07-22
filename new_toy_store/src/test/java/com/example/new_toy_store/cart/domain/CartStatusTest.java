package com.example.new_toy_store.cart.domain;

import com.example.new_toy_store.cart.domain.event.CartStatusChangedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartStatusTest {

    @Test
    void shouldAllowOnlyRealStatusTransitions() {
        assertTrue(CartStatus.ACTIVE.canTransitionTo(CartStatus.CHECKING_OUT));
        assertTrue(CartStatus.CHECKING_OUT.canTransitionTo(CartStatus.ACTIVE));
        assertFalse(CartStatus.ACTIVE.canTransitionTo(CartStatus.ACTIVE));
        assertFalse(CartStatus.CHECKING_OUT.canTransitionTo(CartStatus.CHECKING_OUT));
        assertFalse(CartStatus.ACTIVE.canTransitionTo(null));
    }

    @Test
    void shouldRejectStatusEventWithoutAnActualTransition() {
        assertThrows(IllegalArgumentException.class, () -> new CartStatusChangedEvent(
                1,
                10,
                CartStatus.ACTIVE,
                CartStatus.ACTIVE,
                Instant.now()
        ));
    }
}
