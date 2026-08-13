package com.example.new_toy_store.accounting.application;

import com.example.new_toy_store.accounting.application.dto.request.JournalLineRequest;
import com.example.new_toy_store.accounting.application.dto.request.ManualJournalEntryRequest;
import com.example.new_toy_store.accounting.application.dto.response.AccountBalanceResponse;
import com.example.new_toy_store.accounting.application.dto.response.AccountingDashboardResponse;
import com.example.new_toy_store.accounting.application.dto.response.GeneralLedgerLineResponse;
import com.example.new_toy_store.accounting.application.dto.response.IncomeStatementResponse;
import com.example.new_toy_store.accounting.application.dto.response.JournalEntryResponse;
import com.example.new_toy_store.accounting.application.dto.response.JournalLineResponse;
import com.example.new_toy_store.accounting.application.dto.response.TrialBalanceResponse;
import com.example.new_toy_store.accounting.domain.AccountType;
import com.example.new_toy_store.accounting.domain.AccountingSourceType;
import com.example.new_toy_store.accounting.domain.JournalEntry;
import com.example.new_toy_store.accounting.domain.JournalEntryLine;
import com.example.new_toy_store.accounting.domain.JournalEntryLineRepository;
import com.example.new_toy_store.accounting.domain.JournalEntryRepository;
import com.example.new_toy_store.accounting.domain.JournalEntryStatus;
import com.example.new_toy_store.accounting.domain.LedgerAccount;
import com.example.new_toy_store.accounting.domain.LedgerAccountRepository;
import com.example.new_toy_store.accounting.domain.exception.InvalidJournalEntryException;
import com.example.new_toy_store.accounting.domain.exception.JournalEntryNotFoundException;
import com.example.new_toy_store.accounting.domain.exception.LedgerAccountNotFoundException;
import com.example.new_toy_store.supplier_payment.application.dto.response.SupplierPayableSummary;
import com.example.new_toy_store.supplier_payment.application.facade.SupplierPaymentFacade;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class AccountingService {
    private static final EnumSet<AccountingSourceType> MANUAL_SOURCE_TYPES = EnumSet.of(
            AccountingSourceType.OPENING_BALANCE,
            AccountingSourceType.OWNER_CAPITAL,
            AccountingSourceType.OPERATING_EXPENSE,
            AccountingSourceType.FUND_TRANSFER,
            AccountingSourceType.MANUAL_ADJUSTMENT
    );

    private final LedgerAccountRepository accountRepository;
    private final JournalEntryRepository entryRepository;
    private final JournalEntryLineRepository lineRepository;
    private final SupplierPaymentFacade supplierPaymentFacade;

    public AccountingService(
            LedgerAccountRepository accountRepository,
            JournalEntryRepository entryRepository,
            JournalEntryLineRepository lineRepository,
            SupplierPaymentFacade supplierPaymentFacade
    ) {
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
        this.supplierPaymentFacade = supplierPaymentFacade;
    }

    @Transactional(readOnly = true)
    public List<AccountBalanceResponse> getAccountBalances(LocalDate asOf) {
        LocalDate effectiveDate = asOf == null ? LocalDate.now() : asOf;
        Map<Integer, AmountTotals> totals = toTotalsMap(lineRepository.summarizeBalancesAsOf(effectiveDate));
        return accountRepository.findAllByActiveTrueOrderByCodeAsc().stream()
                .map(account -> toAccountBalance(account, totals.getOrDefault(account.getId(), AmountTotals.ZERO)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<JournalEntryResponse> getJournalEntries(Pageable pageable) {
        return entryRepository.findAll(pageable).map(this::toEntryResponse);
    }

    @Transactional(readOnly = true)
    public JournalEntryResponse getJournalEntry(Integer id) {
        return toEntryResponse(getEntry(id));
    }

    @Transactional(readOnly = true)
    public Page<GeneralLedgerLineResponse> getGeneralLedger(
            String accountCode, LocalDate from, LocalDate to, Pageable pageable
    ) {
        account(accountCode);
        LocalDate effectiveFrom = from == null ? LocalDate.of(2000, 1, 1) : from;
        LocalDate effectiveTo = to == null ? LocalDate.now() : to;
        validatePeriod(effectiveFrom, effectiveTo);
        return lineRepository.findGeneralLedger(accountCode, effectiveFrom, effectiveTo, pageable)
                .map(line -> new GeneralLedgerLineResponse(
                        line.getId(), line.getJournalEntry().getId(), line.getJournalEntry().getEntryNumber(),
                        line.getJournalEntry().getEntryDate(),
                        line.getDescription() == null ? line.getJournalEntry().getDescription() : line.getDescription(),
                        line.getJournalEntry().getStatus(), line.getDebitAmount(), line.getCreditAmount()
                ));
    }

    @Transactional(readOnly = true)
    public TrialBalanceResponse getTrialBalance(LocalDate asOf) {
        LocalDate effectiveDate = asOf == null ? LocalDate.now() : asOf;
        List<AccountBalanceResponse> accounts = getAccountBalances(effectiveDate);
        double totalDebit = round(accounts.stream().mapToDouble(AccountBalanceResponse::totalDebit).sum());
        double totalCredit = round(accounts.stream().mapToDouble(AccountBalanceResponse::totalCredit).sum());
        return new TrialBalanceResponse(effectiveDate, totalDebit, totalCredit, Math.abs(totalDebit - totalCredit) < 0.01, accounts);
    }

    @Transactional(readOnly = true)
    public IncomeStatementResponse getIncomeStatement(LocalDate from, LocalDate to) {
        LocalDate effectiveFrom = from == null ? LocalDate.now().withDayOfMonth(1) : from;
        LocalDate effectiveTo = to == null ? LocalDate.now() : to;
        validatePeriod(effectiveFrom, effectiveTo);

        Map<Integer, AmountTotals> totals = toTotalsMap(lineRepository.summarizeBalancesBetween(effectiveFrom, effectiveTo));
        List<LedgerAccount> accounts = accountRepository.findAllByActiveTrueOrderByCodeAsc();
        double grossRevenue = creditMinusDebit(accounts, totals, account -> account.getCode().equals("511"));
        double salesReturns = debitMinusCredit(accounts, totals, account -> account.getCode().equals("521"));
        double netRevenue = round(grossRevenue - salesReturns);
        double cogs = debitMinusCredit(accounts, totals, account -> account.getCode().equals("632"));
        double operatingExpenses = debitMinusCredit(accounts, totals,
                account -> account.getAccountType() == AccountType.EXPENSE && !account.getCode().equals("632"));
        double totalExpenses = round(cogs + operatingExpenses);
        return new IncomeStatementResponse(
                effectiveFrom, effectiveTo, grossRevenue, salesReturns, netRevenue,
                cogs, operatingExpenses, totalExpenses, round(netRevenue - totalExpenses)
        );
    }

    @Transactional(readOnly = true)
    public AccountingDashboardResponse getDashboard(LocalDate asOf, double minimumCashReserve) {
        LocalDate effectiveDate = asOf == null ? LocalDate.now() : asOf;
        Map<String, AccountBalanceResponse> balances = getAccountBalances(effectiveDate).stream()
                .collect(Collectors.toMap(AccountBalanceResponse::code, item -> item));
        SupplierPayableSummary payable = supplierPaymentFacade.getPayableSummary();
        IncomeStatementResponse income = getIncomeStatement(LocalDate.of(2000, 1, 1), effectiveDate);

        double cash = balanceOf(balances, "111");
        double paymentAccount = balanceOf(balances, "112");
        double liquid = round(cash + paymentAccount);
        double reserve = round(Math.max(0.0, minimumCashReserve));
        double availableAfterPayables = round(liquid - payable.totalOutstanding());
        double safeCapacity = round(Math.max(0.0, liquid - reserve));

        return new AccountingDashboardResponse(
                effectiveDate, cash, paymentAccount, liquid, balanceOf(balances, "156"), balanceOf(balances, "331"),
                payable.totalOutstanding(), payable.overdueOutstanding(), payable.openInvoiceCount(), payable.overdueInvoiceCount(),
                reserve, availableAfterPayables, safeCapacity, income.netRevenue(), income.totalExpenses(), income.netProfit(),
                LocalDateTime.now()
        );
    }

    @Transactional
    public JournalEntryResponse createManualEntry(ManualJournalEntryRequest request, String postedBy) {
        if (!MANUAL_SOURCE_TYPES.contains(request.getSourceType())) {
            throw new InvalidJournalEntryException(
                    "Loại nguồn này chỉ được ghi tự động từ nghiệp vụ gốc.",
                    Map.of("sourceType", request.getSourceType())
            );
        }
        if (entryRepository.existsBySourceTypeAndSourceReference(request.getSourceType(), request.getSourceReference())) {
            throw new InvalidJournalEntryException(
                    "Mã tham chiếu đã được sử dụng cho một bút toán khác.",
                    Map.of("sourceType", request.getSourceType(), "sourceReference", request.getSourceReference())
            );
        }
        List<PostingLine> lines = request.getLines().stream()
                .map(this::toPostingLine)
                .toList();
        return toEntryResponse(post(request.getSourceType(), request.getSourceReference(), request.getEntryDate(),
                request.getDescription(), postedBy, lines));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public JournalEntryResponse postAutomatic(
            AccountingSourceType sourceType,
            String sourceReference,
            LocalDate entryDate,
            String description,
            List<PostingLine> lines
    ) {
        if (entryRepository.existsBySourceTypeAndSourceReference(sourceType, sourceReference)) {
            return entryRepository.findBySourceTypeAndSourceReference(sourceType, sourceReference)
                    .map(this::toEntryResponse)
                    .orElse(null);
        }
        try {
            return toEntryResponse(post(sourceType, sourceReference, entryDate, description, "SYSTEM", lines));
        } catch (DataIntegrityViolationException ignored) {
            return entryRepository.findBySourceTypeAndSourceReference(sourceType, sourceReference)
                    .map(this::toEntryResponse)
                    .orElse(null);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean postAutomaticIfMissing(
            AccountingSourceType sourceType,
            String sourceReference,
            LocalDate entryDate,
            String description,
            List<PostingLine> lines
    ) {
        if (entryRepository.existsBySourceTypeAndSourceReference(sourceType, sourceReference)) {
            return false;
        }
        post(sourceType, sourceReference, entryDate, description, "SYSTEM-RECONCILIATION", lines);
        entryRepository.flush();
        return true;
    }

    @Transactional
    public JournalEntryResponse reverse(Integer id, String postedBy) {
        JournalEntry original = getEntry(id);
        if (original.getStatus() == JournalEntryStatus.REVERSED) {
            throw new InvalidJournalEntryException("Bút toán đã được đảo trước đó.", Map.of("journalEntryId", id));
        }
        List<PostingLine> reversalLines = original.getLines().stream()
                .map(line -> new PostingLine(
                        line.getAccount().getCode(), "Đảo: " + safeDescription(line.getDescription()),
                        line.getCreditAmount(), line.getDebitAmount()
                ))
                .toList();
        JournalEntry reversal = post(
                AccountingSourceType.REVERSAL, "REV-" + id, LocalDate.now(),
                "Đảo bút toán " + original.getEntryNumber(), postedBy, reversalLines
        );
        reversal.reverseEntryOf(original);
        original.markReversed();
        entryRepository.save(original);
        entryRepository.save(reversal);
        return toEntryResponse(reversal);
    }

    private JournalEntry post(
            AccountingSourceType sourceType, String sourceReference, LocalDate entryDate,
            String description, String postedBy, List<PostingLine> lines
    ) {
        JournalEntry entry = new JournalEntry(
                generateEntryNumber(), entryDate, description, sourceType, sourceReference, postedBy
        );
        for (PostingLine line : lines) {
            entry.addLine(new JournalEntryLine(
                    account(line.accountCode()), line.description(), line.debitAmount(), line.creditAmount()
            ));
        }
        entry.validateBalanced();
        return entryRepository.save(entry);
    }

    private PostingLine toPostingLine(JournalLineRequest line) {
        return new PostingLine(line.getAccountCode(), line.getDescription(), line.getDebitAmount(), line.getCreditAmount());
    }

    private LedgerAccount account(String code) {
        return accountRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new LedgerAccountNotFoundException(code));
    }

    private JournalEntry getEntry(Integer id) {
        return entryRepository.findDetailsById(id).orElseThrow(() -> new JournalEntryNotFoundException(id));
    }

    private JournalEntryResponse toEntryResponse(JournalEntry entry) {
        List<JournalLineResponse> lines = entry.getLines().stream()
                .map(line -> new JournalLineResponse(
                        line.getId(), line.getAccount().getCode(), line.getAccount().getName(), line.getDescription(),
                        line.getDebitAmount(), line.getCreditAmount()
                ))
                .toList();
        return new JournalEntryResponse(
                entry.getId(), entry.getEntryNumber(), entry.getEntryDate(), entry.getDescription(),
                entry.getSourceType(), entry.getSourceReference(), entry.getStatus(), entry.getPostedBy(),
                entry.getReversedEntry() == null ? null : entry.getReversedEntry().getId(),
                entry.getTotalDebit(), entry.getTotalCredit(), entry.getCreatedAt(), lines
        );
    }

    private AccountBalanceResponse toAccountBalance(LedgerAccount account, AmountTotals totals) {
        return new AccountBalanceResponse(
                account.getId(), account.getCode(), account.getName(), account.getAccountType(), account.getNormalBalance(),
                account.isLiquidAccount(), totals.debit(), totals.credit(), account.calculateBalance(totals.debit(), totals.credit())
        );
    }

    private Map<Integer, AmountTotals> toTotalsMap(List<Object[]> rows) {
        Map<Integer, AmountTotals> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put(((Number) row[0]).intValue(), new AmountTotals(
                    round(((Number) row[1]).doubleValue()), round(((Number) row[2]).doubleValue())
            ));
        }
        return result;
    }

    private double debitMinusCredit(List<LedgerAccount> accounts, Map<Integer, AmountTotals> totals, Predicate<LedgerAccount> predicate) {
        return round(accounts.stream().filter(predicate).mapToDouble(account -> {
            AmountTotals amount = totals.getOrDefault(account.getId(), AmountTotals.ZERO);
            return amount.debit() - amount.credit();
        }).sum());
    }

    private double creditMinusDebit(List<LedgerAccount> accounts, Map<Integer, AmountTotals> totals, Predicate<LedgerAccount> predicate) {
        return round(accounts.stream().filter(predicate).mapToDouble(account -> {
            AmountTotals amount = totals.getOrDefault(account.getId(), AmountTotals.ZERO);
            return amount.credit() - amount.debit();
        }).sum());
    }

    private double balanceOf(Map<String, AccountBalanceResponse> balances, String code) {
        return balances.containsKey(code) ? balances.get(code).balance() : 0.0;
    }

    private void validatePeriod(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new InvalidJournalEntryException("Ngày bắt đầu không được sau ngày kết thúc.", Map.of("from", from, "to", to));
        }
    }

    private String generateEntryNumber() {
        return "JE-" + LocalDate.now().toString().replace("-", "") + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String safeDescription(String value) { return value == null || value.isBlank() ? "dòng bút toán" : value; }
    private double round(double value) { return Math.round(value * 100.0) / 100.0; }

    public record PostingLine(String accountCode, String description, double debitAmount, double creditAmount) {
        public static PostingLine debit(String code, String description, double amount) {
            return new PostingLine(code, description, amount, 0.0);
        }
        public static PostingLine credit(String code, String description, double amount) {
            return new PostingLine(code, description, 0.0, amount);
        }
    }

    private record AmountTotals(double debit, double credit) {
        private static final AmountTotals ZERO = new AmountTotals(0.0, 0.0);
    }
}
