package com.example.new_toy_store.cart.domain.exception;

public class CartCrossModuleException extends CartDomainException {
    public CartCrossModuleException(String targetModule, String operation, String reason) {
        super("Lỗi giao tiếp nghiệp vụ với module [" + targetModule + "]: " + reason, "CROSS_MODULE_ERROR");
        addContext("targetModule", targetModule);
        addContext("operation", operation);
        addContext("reason", reason);
    }
}