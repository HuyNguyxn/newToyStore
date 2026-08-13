package com.example.new_toy_store.accounting.api;

import com.example.new_toy_store.accounting.application.AccountingFacade;
import com.example.new_toy_store.accounting.application.dto.request.ManualJournalEntryRequest;
import com.example.new_toy_store.accounting.application.dto.response.AccountBalanceResponse;
import com.example.new_toy_store.accounting.application.dto.response.AccountingDashboardResponse;
import com.example.new_toy_store.accounting.application.dto.response.GeneralLedgerLineResponse;
import com.example.new_toy_store.accounting.application.dto.response.IncomeStatementResponse;
import com.example.new_toy_store.accounting.application.dto.response.JournalEntryResponse;
import com.example.new_toy_store.accounting.application.dto.response.TrialBalanceResponse;
import com.example.new_toy_store.accounting.application.dto.response.AccountingReconciliationResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/accounting")
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class AccountingController {
    private final AccountingFacade facade;

    public AccountingController(AccountingFacade facade) { this.facade = facade; }

    @GetMapping("/dashboard")
    public AccountingDashboardResponse dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf,
            @RequestParam(defaultValue = "0") double minimumReserve
    ) {
        return facade.getDashboard(asOf, minimumReserve);
    }

    @GetMapping("/accounts")
    public List<AccountBalanceResponse> accounts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf
    ) {
        return facade.getAccounts(asOf);
    }

    @GetMapping("/journal-entries")
    public Page<JournalEntryResponse> journalEntries(Pageable pageable) { return facade.getJournalEntries(pageable); }

    @GetMapping("/journal-entries/{id}")
    public JournalEntryResponse journalEntry(@PathVariable Integer id) { return facade.getJournalEntry(id); }

    @GetMapping("/general-ledger/{accountCode}")
    public Page<GeneralLedgerLineResponse> generalLedger(
            @PathVariable String accountCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable
    ) {
        return facade.getGeneralLedger(accountCode, from, to, pageable);
    }

    @GetMapping("/reports/trial-balance")
    public TrialBalanceResponse trialBalance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate asOf
    ) {
        return facade.getTrialBalance(asOf);
    }

    @GetMapping("/reports/income-statement")
    public IncomeStatementResponse incomeStatement(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return facade.getIncomeStatement(from, to);
    }

    @GetMapping("/reconciliation/preview")
    public AccountingReconciliationResponse previewReconciliation() {
        return facade.previewReconciliation();
    }

    @PostMapping("/reconciliation/execute")
    @PreAuthorize("hasRole('ADMIN')")
    public AccountingReconciliationResponse executeReconciliation() {
        return facade.executeReconciliation();
    }

    @PostMapping("/journal-entries")
    @PreAuthorize("hasRole('ADMIN')")
    public JournalEntryResponse createManualEntry(
            @Valid @RequestBody ManualJournalEntryRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        return facade.createManualEntry(request, user == null ? "ADMIN" : user.getUsername());
    }

    @PostMapping("/journal-entries/{id}/reverse")
    @PreAuthorize("hasRole('ADMIN')")
    public JournalEntryResponse reverse(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user
    ) {
        return facade.reverse(id, user == null ? "ADMIN" : user.getUsername());
    }
}
