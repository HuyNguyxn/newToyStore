package com.example.new_toy_store.global.event;

public record OrderCancelledItemPayload(
        Integer variantId,
        int quantity
) {
}
