package com.example.new_toy_store.global.event;

public record OrderCreatedItemPayload(
        Integer variantId,
        int quantity
) {
}
