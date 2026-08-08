package com.example.new_toy_store.global.event;

public record ImportNoteCompletedItemPayload(
        Integer variantId,
        int quantity,
        double importPrice,
        String batchNumber
) {
}
