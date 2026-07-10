package com.example.new_toy_store.order.domain.exception;

public class InsufficientStockException extends RuntimeException {
    private final Integer productId;
    private final String productName;
    private final int requestedQuantity;
    private final int availableQuantity;

    public InsufficientStockException(Integer productId, String productName, int requestedQuantity, int availableQuantity) {
        super(String.format("Sản phẩm '%s' chỉ còn %d chiếc, không đủ %d chiếc theo yêu cầu.", productName, availableQuantity, requestedQuantity));
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