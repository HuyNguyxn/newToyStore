package com.example.new_toy_store.accounting.application.dto.response;

import java.time.LocalDate;

public record IncomeStatementResponse(
        LocalDate from,
        LocalDate to,
        double grossSalesRevenue,
        double salesReturns,
        double netRevenue,
        double costOfGoodsSold,
        double operatingExpenses,
        double totalExpenses,
        double netProfit
) {}
