package com.example.new_toy_store.order.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InsufficientStockException extends OrderDomainException {

    private final Integer productId;
    private final String productName;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(Integer productId, String productName, int requestedQuantity, int availableQuantity) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "ORDER_INSUFFICIENT_STOCK",
                "Sản phẩm '" + productName + "' chỉ còn " + availableQuantity + " sản phẩm, không đủ " + requestedQuantity + " sản phẩm theo yêu cầu.",
                Map.of(
                        "productId", productId,
                        "productName", productName,
                        "requestedQuantity", requestedQuantity,
                        "availableQuantity", availableQuantity
                )
        );
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    public Integer getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getRequestedQuantity() { return requestedQuantity; }
    public int getAvailableQuantity() { return availableQuantity; }
}
