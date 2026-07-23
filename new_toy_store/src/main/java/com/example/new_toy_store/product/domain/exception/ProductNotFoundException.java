package com.example.new_toy_store.product.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ProductNotFoundException extends ProductDomainException {

    public ProductNotFoundException(Integer id) {
        super(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy sản phẩm",
                "Không tìm thấy sản phẩm với ID: " + id,
                Map.of("productId", id == null ? "" : id)
        );
    }

    public ProductNotFoundException(String message) {
        super(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy dữ liệu sản phẩm",
                message
        );
    }
}
