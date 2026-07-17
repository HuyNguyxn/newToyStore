package com.example.new_toy_store.product.application;

import com.example.new_toy_store.product.application.dto.response.InventoryBatchResponse;
import com.example.new_toy_store.product.domain.Inventory;
import com.example.new_toy_store.product.domain.InventoryRepository;
import com.example.new_toy_store.product.domain.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional(readOnly = true)
    public List<InventoryBatchResponse> getAvailableBatchesForVariant(Integer variantId) {
        Inventory inventory = inventoryRepository.findByVariantId(variantId)
                .orElseThrow(() -> new ProductNotFoundException("Không tìm thấy dữ liệu tồn kho cho biến thể ID: " + variantId));

        return inventory.getBatches().stream()
                .filter(batch -> batch.getQuantity() > 0)
                .map(batch -> new InventoryBatchResponse(
                        batch.getBatchNumber(),
                        batch.getExpiryDate(),
                        batch.getQuantity()
                ))
                .collect(Collectors.toList());
    }
}