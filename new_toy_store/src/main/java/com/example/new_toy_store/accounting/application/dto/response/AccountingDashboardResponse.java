package com.example.new_toy_store.accounting.application.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AccountingDashboardResponse(
        LocalDate asOf,
        double cashBalance,
        double paymentAccountBalance,
        double totalLiquidFunds,
        double inventoryValue,
        double ledgerAccountsPayable,
        double supplierOutstanding,
        double supplierOverdue,
        long openSupplierInvoiceCount,
        long overdueSupplierInvoiceCount,
        double minimumCashReserve,
        double availableAfterPayables,
        double safeSupplierPaymentCapacity,
        double netRevenue,
        double totalExpenses,
        double netProfit,
        LocalDateTime generatedAt
) {}
