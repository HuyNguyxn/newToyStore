package com.example.new_toy_store.cart.domain.exception;

public class CartDataConflictException extends CartDomainException {

    public enum ConflictReason {
        SOFT_DELETED_PRODUCT("Sản phẩm đã ngừng kinh doanh hoặc bị xóa khỏi hệ thống."),
        DUPLICATE_ACTIVE_ITEM("Sản phẩm đã tồn tại và đang hoạt động trong giỏ hàng."),
        PRICE_CHANGED("Giá sản phẩm đã thay đổi. Vui lòng kiểm tra lại trước khi thanh toán.");

        private final String description;

        ConflictReason(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private CartDataConflictException(ConflictReason reason) {
        super("Xung đột dữ liệu giỏ hàng: " + reason.getDescription(), "CART_DATA_CONFLICT");
        addContext("reason", reason.name());
    }

    public static CartDataConflictException softDeletedProduct(Integer productId) {
        CartDataConflictException exception = new CartDataConflictException(ConflictReason.SOFT_DELETED_PRODUCT);
        exception.addContext("productId", productId);
        return exception;
    }

    public static CartDataConflictException duplicateActiveItem(Integer cartId, Integer productId, Integer variantId) {
        CartDataConflictException exception = new CartDataConflictException(ConflictReason.DUPLICATE_ACTIVE_ITEM);
        exception.addContext("cartId", cartId);
        exception.addContext("productId", productId);
        exception.addContext("variantId", variantId);
        return exception;
    }

    public static CartDataConflictException priceChanged(Integer productId, Integer variantId,
                                                        double oldPrice, double newPrice) {
        CartDataConflictException exception = new CartDataConflictException(ConflictReason.PRICE_CHANGED);
        exception.addContext("productId", productId);
        exception.addContext("variantId", variantId);
        exception.addContext("oldPrice", oldPrice);
        exception.addContext("newPrice", newPrice);
        return exception;
    }
}
