package com.example.new_toy_store.cart.domain.exception;

public class CartDataConflictException extends CartDomainException {

    public enum ConflictReason {
        SOFT_DELETED_PRODUCT("Xung đột dữ liệu: Sản phẩm này đã ngừng kinh doanh hoặc bị xóa khỏi hệ thống."),
        DUPLICATE_ACTIVE_ITEM("Xung đột dữ liệu: Sản phẩm này đã tồn tại và đang hoạt động trong giỏ hàng."),
        PRICE_CHANGED("Xung đột dữ liệu: Giá sản phẩm đã thay đổi. Vui lòng kiểm tra lại trước khi thanh toán.");

        private final String description;

        ConflictReason(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    private CartDataConflictException(ConflictReason reason, Integer productId) {
        super(reason.getDescription(), "DATA_CONFLICT");
        addContext("reason", reason.name());
        addContext("productId", productId);
    }

    public static CartDataConflictException softDeletedProduct(Integer productId) {
        return new CartDataConflictException(ConflictReason.SOFT_DELETED_PRODUCT, productId);
    }

    public static CartDataConflictException duplicateItem(Integer productId) {
        return new CartDataConflictException(ConflictReason.DUPLICATE_ACTIVE_ITEM, productId);
    }

    public static CartDataConflictException priceChanged(Integer productId, double oldPrice, double newPrice) {
        CartDataConflictException ex = new CartDataConflictException(ConflictReason.PRICE_CHANGED, productId);
        ex.addContext("oldPrice", oldPrice);
        ex.addContext("newPrice", newPrice);
        return ex;
    }
}