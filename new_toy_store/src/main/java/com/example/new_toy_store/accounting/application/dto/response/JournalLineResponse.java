package com.example.new_toy_store.accounting.application.dto.response;

public record JournalLineResponse(
        Integer id,
        String accountCode,
        String accountName,
        String description,
        double debitAmount,
        double creditAmount
) {}
