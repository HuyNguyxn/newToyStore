package com.example.new_toy_store.accounting.application.dto.response;

import com.example.new_toy_store.accounting.domain.JournalEntryStatus;

import java.time.LocalDate;

public record GeneralLedgerLineResponse(
        Integer lineId,
        Integer journalEntryId,
        String entryNumber,
        LocalDate entryDate,
        String description,
        JournalEntryStatus status,
        double debitAmount,
        double creditAmount
) {}
