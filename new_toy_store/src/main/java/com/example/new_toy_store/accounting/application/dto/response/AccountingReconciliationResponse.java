package com.example.new_toy_store.accounting.application.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record AccountingReconciliationResponse(
        boolean executed,
        int detectedCount,
        int createdCount,
        int skippedCount,
        double detectedAmount,
        List<AccountingReconciliationGroupResponse> groups,
        LocalDateTime generatedAt
) {}
