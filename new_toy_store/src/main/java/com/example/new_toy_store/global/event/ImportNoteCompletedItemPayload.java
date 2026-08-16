package com.example.new_toy_store.global.event;

public record ImportNoteCompletedItemPayload(
        Integer productId,
        Integer variantId,
        int quantity,
        double importPrice,
        Double sellingPrice,
        String batchNumber
) {
}
