package com.example.new_toy_store.accounting.application.dto.response;

import java.time.LocalDate;
import java.util.List;

public record TrialBalanceResponse(
        LocalDate asOf,
        double totalDebit,
        double totalCredit,
        boolean balanced,
        List<AccountBalanceResponse> accounts
) {}
