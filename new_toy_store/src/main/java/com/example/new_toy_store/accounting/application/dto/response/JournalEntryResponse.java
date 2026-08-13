package com.example.new_toy_store.accounting.application.dto.response;

import com.example.new_toy_store.accounting.domain.AccountingSourceType;
import com.example.new_toy_store.accounting.domain.JournalEntryStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record JournalEntryResponse(
        Integer id,
        String entryNumber,
        LocalDate entryDate,
        String description,
        AccountingSourceType sourceType,
        String sourceReference,
        JournalEntryStatus status,
        String postedBy,
        Integer reversedEntryId,
        double totalDebit,
        double totalCredit,
        LocalDateTime createdAt,
        List<JournalLineResponse> lines
) {}
