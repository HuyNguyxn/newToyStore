package com.example.new_toy_store.global.event;

import java.time.Instant;
import java.util.List;

public class SupplierReturnCompletedEvent {

    private final Integer returnId;
    private final Integer supplierId;
    private final List<ReturnItemDetail> items;
    private final double inventoryAmount;
    private final double refundAmount;
    private final Instant occurredAt;

    public SupplierReturnCompletedEvent(
            Integer returnId,
            Integer supplierId,
            List<ReturnItemDetail> items,
            double inventoryAmount,
            double refundAmount
    ) {
        this.returnId = returnId;
        this.supplierId = supplierId;
        this.items = items;
        this.inventoryAmount = inventoryAmount;
        this.refundAmount = refundAmount;
        this.occurredAt = Instant.now();
    }

    public Integer getReturnId() {
        return returnId;
    }

    public List<ReturnItemDetail> getItems() {
        return items;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public double getInventoryAmount() {
        return inventoryAmount;
    }

    public double getRefundAmount() {
        return refundAmount;
    }

    public Instant getOccurredAt() {
        return occurredAt;
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
