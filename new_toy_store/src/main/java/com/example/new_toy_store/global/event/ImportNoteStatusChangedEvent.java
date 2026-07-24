package com.example.new_toy_store.global.event;

import com.example.new_toy_store.imports.domain.ImportStatus;

import java.time.Instant;

public record ImportNoteStatusChangedEvent(
        Integer importNoteId,
        Integer supplierId,
        ImportStatus previousStatus,
        ImportStatus currentStatus,
        Instant occurredAt
) {
    public static ImportNoteStatusChangedEvent now(
            Integer importNoteId,
            Integer supplierId,
            ImportStatus previousStatus,
            ImportStatus currentStatus
    ) {
        return new ImportNoteStatusChangedEvent(
                importNoteId,
                supplierId,
                previousStatus,
                currentStatus,
                Instant.now()
        );
    }
}
