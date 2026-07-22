package com.example.new_toy_store.global.event;

import java.util.List;

public final class CartCheckoutRequestedEvent {
    private final Integer cartId;
    private final Integer userId;
    private final String shippingAddress;
    private final String promoCode;
    private final List<CartCheckoutItemPayload> items;

    public CartCheckoutRequestedEvent(Integer cartId, Integer userId, String shippingAddress,
                                      String promoCode, List<CartCheckoutItemPayload> items) {
        this.cartId = cartId;
        this.userId = userId;
        this.shippingAddress = shippingAddress;
        this.promoCode = promoCode;
        this.items = List.copyOf(items);
    }

    public Integer getCartId() { return cartId; }
    public Integer getUserId() { return userId; }
    public String getShippingAddress() { return shippingAddress; }
    public String getPromoCode() { return promoCode; }
    public List<CartCheckoutItemPayload> getItems() { return items; }
}
