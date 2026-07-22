package com.example.new_toy_store.cart.domain.exception;

public class CartCrossModuleException extends CartDomainException {

    private CartCrossModuleException(String targetModule, String operation, String reason) {
        super("Lỗi giao tiếp nghiệp vụ giữa Cart và " + targetModule + ": " + reason, "CART_CROSS_MODULE_ERROR");
        addContext("sourceModule", "Cart");
        addContext("targetModule", targetModule);
        addContext("operation", operation);
        addContext("reason", reason);
    }

    public static CartCrossModuleException missingProduct(Integer productId) {
        CartCrossModuleException exception = new CartCrossModuleException(
                "Product",
                "GET_PRODUCT",
                "Không tìm thấy dữ liệu sản phẩm cần dùng cho giỏ hàng."
        );
        exception.addContext("productId", productId);
        return exception;
    }

    public static CartCrossModuleException missingVariant(Integer productId, Integer variantId) {
        CartCrossModuleException exception = new CartCrossModuleException(
                "Product",
                "GET_VARIANT",
                "Không tìm thấy phân loại sản phẩm tương ứng với dữ liệu trong giỏ hàng."
        );
        exception.addContext("productId", productId);
        exception.addContext("variantId", variantId);
        return exception;
    }

    public static CartCrossModuleException invalidInventory(Integer productId, Integer variantId) {
        CartCrossModuleException exception = new CartCrossModuleException(
                "Inventory",
                "CHECK_STOCK",
                "Dữ liệu tồn kho của phân loại sản phẩm không hợp lệ hoặc chưa được khởi tạo."
        );
        exception.addContext("productId", productId);
        exception.addContext("variantId", variantId);
        return exception;
    }

    public static CartCrossModuleException insufficientStock(Integer productId, Integer variantId,
                                                            int requestedQuantity, int availableQuantity) {
        CartCrossModuleException exception = new CartCrossModuleException(
                "Inventory",
                "CHECK_STOCK",
                "Số lượng tồn kho không đủ để đáp ứng yêu cầu."
        );
        exception.addContext("productId", productId);
        exception.addContext("variantId", variantId);
        exception.addContext("requestedQuantity", requestedQuantity);
        exception.addContext("availableQuantity", availableQuantity);
        return exception;
    }
}
