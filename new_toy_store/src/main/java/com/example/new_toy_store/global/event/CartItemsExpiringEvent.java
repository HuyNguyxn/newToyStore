package com.example.new_toy_store.global.event;

import java.util.List;

public final class CartItemsExpiringEvent {

    private final int daysLeft;
    private final List<Item> items;

    public CartItemsExpiringEvent(int daysLeft, List<Item> items) {
        this.daysLeft = daysLeft;
        this.items = List.copyOf(items);
    }

    public int getDaysLeft() {
        return daysLeft;
    }

    public List<Item> getItems() {
        return items;
    }

    public record Item(Integer userId, Integer productId, Integer variantId) {
    }
}
