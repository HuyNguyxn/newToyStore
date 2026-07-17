package com.example.new_toy_store.product.api;

import com.example.new_toy_store.product.application.InventoryService;
import com.example.new_toy_store.product.application.dto.response.InventoryBatchResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/variants/{variantId}/batches")
    public ResponseEntity<List<InventoryBatchResponse>> getAvailableBatchesForVariant(@PathVariable Integer variantId) {
        List<InventoryBatchResponse> batches = inventoryService.getAvailableBatchesForVariant(variantId);
        return ResponseEntity.ok(batches);
    }
}