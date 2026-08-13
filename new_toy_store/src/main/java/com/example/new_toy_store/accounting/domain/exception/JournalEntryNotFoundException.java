package com.example.new_toy_store.accounting.domain.exception;

import java.util.Map;

public class JournalEntryNotFoundException extends AccountingDomainException {
    public JournalEntryNotFoundException(Integer id) {
        super("JOURNAL_ENTRY_NOT_FOUND", "Không tìm thấy bút toán #" + id + ".", Map.of("journalEntryId", id));
    }
}
