package com.example.new_toy_store.accounting.application.dto.response;

import com.example.new_toy_store.accounting.domain.AccountingSourceType;

public record AccountingReconciliationGroupResponse(
        AccountingSourceType sourceType,
        int missingCount,
        double totalAmount
) {}
