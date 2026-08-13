package com.example.new_toy_store.accounting.application;

import com.example.new_toy_store.accounting.application.dto.request.ManualJournalEntryRequest;
import com.example.new_toy_store.accounting.application.dto.response.AccountBalanceResponse;
import com.example.new_toy_store.accounting.application.dto.response.AccountingDashboardResponse;
import com.example.new_toy_store.accounting.application.dto.response.GeneralLedgerLineResponse;
import com.example.new_toy_store.accounting.application.dto.response.IncomeStatementResponse;
import com.example.new_toy_store.accounting.application.dto.response.JournalEntryResponse;
import com.example.new_toy_store.accounting.application.dto.response.TrialBalanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class AccountingFacade {
    private final AccountingService service;

    public AccountingFacade(AccountingService service) { this.service = service; }

    public AccountingDashboardResponse getDashboard(LocalDate asOf, double minimumReserve) {
        return service.getDashboard(asOf, minimumReserve);
    }
    public List<AccountBalanceResponse> getAccounts(LocalDate asOf) { return service.getAccountBalances(asOf); }
    public Page<JournalEntryResponse> getJournalEntries(Pageable pageable) { return service.getJournalEntries(pageable); }
    public JournalEntryResponse getJournalEntry(Integer id) { return service.getJournalEntry(id); }
    public Page<GeneralLedgerLineResponse> getGeneralLedger(String code, LocalDate from, LocalDate to, Pageable pageable) {
        return service.getGeneralLedger(code, from, to, pageable);
    }
    public TrialBalanceResponse getTrialBalance(LocalDate asOf) { return service.getTrialBalance(asOf); }
    public IncomeStatementResponse getIncomeStatement(LocalDate from, LocalDate to) { return service.getIncomeStatement(from, to); }
    public JournalEntryResponse createManualEntry(ManualJournalEntryRequest request, String postedBy) {
        return service.createManualEntry(request, postedBy);
    }
    public JournalEntryResponse reverse(Integer id, String postedBy) { return service.reverse(id, postedBy); }
}
