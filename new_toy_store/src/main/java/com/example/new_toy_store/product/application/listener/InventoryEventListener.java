package com.example.new_toy_store.product.application.listener;

import com.example.new_toy_store.global.event.ImportNoteCompletedEvent;
import com.example.new_toy_store.global.event.SupplierReturnCompletedEvent;
import com.example.new_toy_store.global.event.SupplierReturnStockRestorationRequestedEvent;
import com.example.new_toy_store.product.application.dto.request.ImportedStockRequest;
import com.example.new_toy_store.product.application.service.ProductService;
import com.example.new_toy_store.product.domain.Inventory;
import com.example.new_toy_store.product.domain.ProductVariant;
import com.example.new_toy_store.product.domain.ProductVariantRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
public class InventoryEventListener {

    private final ProductVariantRepository variantRepository;
    private final ProductService productService;

    public InventoryEventListener(ProductVariantRepository variantRepository, ProductService productService) {
        this.variantRepository = variantRepository;
        this.productService = productService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onImportNoteCompleted(ImportNoteCompletedEvent event) {
        List<ImportedStockRequest> stockUpdates = event.items().stream()
                .map(item -> new ImportedStockRequest(
                        item.variantId(),
                        item.quantity(),
                        item.importPrice(),
                        item.batchNumber()
                ))
                .toList();

        productService.processImportedStock(stockUpdates);
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

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onSupplierReturnStockRestorationRequested(SupplierReturnStockRestorationRequestedEvent event) {
        event.items().forEach(item -> {
            ProductVariant variant = variantRepository.findById(item.variantId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể sản phẩm ID: " + item.variantId()));
            if (variant.getInventory() == null) {
                throw new RuntimeException("Biến thể ID " + item.variantId() + " chưa được khởi tạo kho.");
            }
            variant.getInventory().addStock(
                    item.quantity(), item.batchNumber(), java.time.LocalDate.now().plusYears(5)
            );
            variantRepository.save(variant);
        });
    }
}
