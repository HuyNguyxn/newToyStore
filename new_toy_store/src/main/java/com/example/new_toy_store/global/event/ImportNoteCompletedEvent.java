package com.example.new_toy_store.global.event;

import java.time.Instant;
import java.util.List;

public record ImportNoteCompletedEvent(
        Integer importNoteId,
        Integer supplierId,
        List<ImportNoteCompletedItemPayload> items,
        Instant occurredAt
) {
    public static ImportNoteCompletedEvent now(
            Integer importNoteId,
            Integer supplierId,
            List<ImportNoteCompletedItemPayload> items
    ) {
        return new ImportNoteCompletedEvent(importNoteId, supplierId, List.copyOf(items), Instant.now());
    }
}
