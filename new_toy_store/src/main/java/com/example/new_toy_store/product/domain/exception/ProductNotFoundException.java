package com.example.new_toy_store.product.domain.exception;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Integer id) {
        super("Không tìm thấy sản phẩm với ID: " + id);
    }
}