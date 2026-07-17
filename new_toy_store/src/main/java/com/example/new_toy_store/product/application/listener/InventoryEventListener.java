package com.example.new_toy_store.product.application.listener;

import com.example.new_toy_store.global.event.SupplierReturnCompletedEvent;
import com.example.new_toy_store.product.domain.Inventory;
import com.example.new_toy_store.product.domain.ProductVariant;
import com.example.new_toy_store.product.domain.ProductVariantRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InventoryEventListener {

    private final ProductVariantRepository variantRepository;

    public InventoryEventListener(ProductVariantRepository variantRepository) {
        this.variantRepository = variantRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onSupplierReturnCompleted(SupplierReturnCompletedEvent event) {
        event.getItems().forEach(item -> {
            ProductVariant variant = variantRepository.findById(item.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm ID: " + item.getVariantId()));

            Inventory inventory = variant.getInventory();
            if (inventory == null) {
                throw new RuntimeException("Biến thể ID " + item.getVariantId() + " chưa được khởi tạo kho.");
            }

            inventory.reduceStockFromBatch(item.getQuantity(), item.getBatchNumber());

            variantRepository.save(variant);
        });
    }
}