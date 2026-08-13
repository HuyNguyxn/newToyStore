package com.example.new_toy_store.accounting.application.dto.response;

import com.example.new_toy_store.accounting.domain.AccountType;
import com.example.new_toy_store.accounting.domain.NormalBalance;

public record AccountBalanceResponse(
        Integer id,
        String code,
        String name,
        AccountType accountType,
        NormalBalance normalBalance,
        boolean liquidAccount,
        double totalDebit,
        double totalCredit,
        double balance
) {}
