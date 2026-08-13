package com.example.new_toy_store.accounting.application.reconciliation;

import com.example.new_toy_store.accounting.application.AccountingService;
import com.example.new_toy_store.accounting.application.AccountingService.PostingLine;
import com.example.new_toy_store.accounting.application.dto.response.AccountingReconciliationGroupResponse;
import com.example.new_toy_store.accounting.application.dto.response.AccountingReconciliationResponse;
import com.example.new_toy_store.accounting.domain.AccountingSourceType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountingReconciliationService {
    private final AccountingReconciliationSourceReader sourceReader;
    private final AccountingService accountingService;

    public AccountingReconciliationService(
            AccountingReconciliationSourceReader sourceReader,
            AccountingService accountingService
    ) {
        this.sourceReader = sourceReader;
        this.accountingService = accountingService;
    }

    public AccountingReconciliationResponse preview() {
        List<Candidate> candidates = findCandidates();
        return response(false, candidates, 0, 0);
    }

    public AccountingReconciliationResponse execute() {
        List<Candidate> candidates = findCandidates();
        int created = 0;
        int skipped = 0;
        for (Candidate candidate : candidates) {
            boolean wasCreated = accountingService.postAutomaticIfMissing(
                    candidate.sourceType(), candidate.sourceReference(), candidate.entryDate(),
                    candidate.description(), candidate.lines()
            );
            if (wasCreated) created++;
            else skipped++;
        }
        return response(true, candidates, created, skipped);
    }

    private List<Candidate> findCandidates() {
        List<Candidate> result = new ArrayList<>();

        sourceReader.findMissingCustomerPayments().forEach(source -> {
            if (source.amount() <= 0.0) return;
            String cashAccount = "COD".equalsIgnoreCase(source.method()) ? "111" : "112";
            result.add(candidate(AccountingSourceType.CUSTOMER_PAYMENT, "PAYMENT-" + source.id(),
                    source.entryDate(), "Thu tiền đơn hàng #" + source.orderId(), source.amount(),
                    PostingLine.debit(cashAccount, "Tiền khách hàng đã thanh toán", source.amount()),
                    PostingLine.credit("3388", "Khoản chờ hoàn tất đơn hàng", source.amount())));
        });

        sourceReader.findMissingImportReceipts().forEach(source -> {
            if (source.amount() <= 0.0) return;
            result.add(candidate(AccountingSourceType.IMPORT_RECEIPT, "IMPORT-" + source.id(),
                        source.entryDate(), "Nhập kho theo phiếu nhập #" + source.id(), source.amount(),
                        PostingLine.debit("156", "Giá trị hàng nhập kho", source.amount()),
                        PostingLine.credit("331", "Công nợ nhà cung cấp #" + source.supplierId(), source.amount())));
        });

        sourceReader.findMissingCompletedOrders().forEach(source -> {
            List<PostingLine> lines = new ArrayList<>();
            if (source.revenueAmount() > 0.0) {
                lines.add(PostingLine.debit("3388", "Kết chuyển khoản đã thu", source.revenueAmount()));
                lines.add(PostingLine.credit("511", "Doanh thu bán hàng", source.revenueAmount()));
            }
            if (source.costAmount() > 0.0) {
                lines.add(PostingLine.debit("632", "Giá vốn hàng bán", source.costAmount()));
                lines.add(PostingLine.credit("156", "Xuất giá trị hàng tồn kho", source.costAmount()));
            }
            if (!lines.isEmpty()) {
                result.add(new Candidate(AccountingSourceType.ORDER_COMPLETION, "ORDER-COMPLETION-" + source.id(),
                        source.entryDate(), "Hoàn tất đơn hàng #" + source.id(),
                        round(Math.max(source.revenueAmount(), source.costAmount())), List.copyOf(lines)));
            }
        });

        sourceReader.findMissingCustomerRefunds().forEach(source -> {
            if (source.amount() <= 0.0) return;
            String debitAccount = "CANCELLED".equalsIgnoreCase(source.orderStatus()) ? "3388" : "521";
            String cashAccount = "COD_MANUAL".equalsIgnoreCase(source.method()) ? "111" : "112";
            result.add(candidate(AccountingSourceType.CUSTOMER_REFUND, "REFUND-" + source.id(),
                    source.entryDate(), "Hoàn tiền đơn hàng #" + source.orderId(), source.amount(),
                    PostingLine.debit(debitAccount, "Khoản hoàn cho khách hàng", source.amount()),
                    PostingLine.credit(cashAccount, "Tiền hoàn cho khách hàng", source.amount())));
        });

        sourceReader.findMissingSupplierPayments().forEach(source -> {
            if (source.amount() <= 0.0) return;
            String cashAccount = "CASH".equalsIgnoreCase(source.method()) ? "111" : "112";
            result.add(candidate(AccountingSourceType.SUPPLIER_PAYMENT, "SUPPLIER-PAYMENT-" + source.id(),
                    source.entryDate(), "Thanh toán nhà cung cấp #" + source.supplierId(), source.amount(),
                    PostingLine.debit("331", "Giảm công nợ phiếu nhập #" + source.importNoteId(), source.amount()),
                    PostingLine.credit(cashAccount, "Chi tiền nhà cung cấp", source.amount())));
        });
        return result;
    }

    private Candidate candidate(
            AccountingSourceType sourceType, String reference, LocalDate date,
            String description, double amount, PostingLine debit, PostingLine credit
    ) {
        return new Candidate(sourceType, reference, date, description, amount, List.of(debit, credit));
    }

    private AccountingReconciliationResponse response(
            boolean executed, List<Candidate> candidates, int created, int skipped
    ) {
        Map<AccountingSourceType, GroupTotals> totals = new LinkedHashMap<>();
        for (Candidate candidate : candidates) {
            totals.compute(candidate.sourceType(), (type, current) -> current == null
                    ? new GroupTotals(1, candidate.amount())
                    : new GroupTotals(current.count() + 1, round(current.amount() + candidate.amount())));
        }
        List<AccountingReconciliationGroupResponse> groups = totals.entrySet().stream()
                .map(entry -> new AccountingReconciliationGroupResponse(
                        entry.getKey(), entry.getValue().count(), round(entry.getValue().amount())))
                .toList();
        double detectedAmount = round(candidates.stream().mapToDouble(Candidate::amount).sum());
        return new AccountingReconciliationResponse(
                executed, candidates.size(), created, skipped, detectedAmount, groups, LocalDateTime.now());
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private record Candidate(
            AccountingSourceType sourceType,
            String sourceReference,
            LocalDate entryDate,
            String description,
            double amount,
            List<PostingLine> lines
    ) {}

    private record GroupTotals(int count, double amount) {}
}
