package com.example.new_toy_store.global.event;

import java.util.List;

public class SupplierReturnCompletedEvent {

    private final Integer returnId;
    private final List<ReturnItemDetail> items;

    public SupplierReturnCompletedEvent(Integer returnId, List<ReturnItemDetail> items) {
        this.returnId = returnId;
        this.items = items;
    }

    public Integer getReturnId() {
        return returnId;
    }

    public List<ReturnItemDetail> getItems() {
        return items;
    }

    public static class ReturnItemDetail {
        private final Integer variantId;
        private final String batchNumber;
        private final int quantity;

        public ReturnItemDetail(Integer variantId, String batchNumber, int quantity) {
            this.variantId = variantId;
            this.batchNumber = batchNumber;
            this.quantity = quantity;
        }

        public Integer getVariantId() {
            return variantId;
        }

        public String getBatchNumber() {
            return batchNumber;
        }

        public int getQuantity() {
            return quantity;
        }
    }
}