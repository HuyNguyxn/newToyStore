package com.example.new_toy_store.global.event;

import java.util.List;

public final class CartCheckoutRequestedEvent {
    private final Integer cartId;
    private final Integer userId;
    private final String shippingAddress;
    private final String promoCode;
    private final List<CheckoutItemDetail> items;

    public CartCheckoutRequestedEvent(Integer cartId, Integer userId, String shippingAddress, String promoCode, List<CheckoutItemDetail> items) {
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
    public List<CheckoutItemDetail> getItems() { return items; }

    public static final class CheckoutItemDetail {
        private final Integer productId;
        private final Integer variantId;
        private final String productName;
        private final String variantAttributesSnapshot;
        private final int quantity;
        private final double price;

        public CheckoutItemDetail(Integer productId, Integer variantId, String productName, String variantAttributesSnapshot, int quantity, double price) {
            this.productId = productId;
            this.variantId = variantId;
            this.productName = productName;
            this.variantAttributesSnapshot = variantAttributesSnapshot;
            this.quantity = quantity;
            this.price = price;
        }

        public Integer getProductId() { return productId; }
        public Integer getVariantId() { return variantId; }
        public String getProductName() { return productName; }
        public String getVariantAttributesSnapshot() { return variantAttributesSnapshot; }
        public int getQuantity() { return quantity; }
        public double getPrice() { return price; }
    }
}
