package com.example.new_toy_store.accounting.application.listener;

import com.example.new_toy_store.accounting.application.AccountingService;
import com.example.new_toy_store.accounting.domain.AccountingSourceType;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentMethod;
import com.example.new_toy_store.customer_payment.domain.RefundMethod;
import com.example.new_toy_store.global.event.ImportNoteCompletedEvent;
import com.example.new_toy_store.global.event.PaymentCompletedEvent;
import com.example.new_toy_store.global.event.PaymentRefundedEvent;
import com.example.new_toy_store.global.event.SupplierPaymentRecordedEvent;
import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentMethod;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Component
public class AccountingEventListener {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private final AccountingService accountingService;

    public AccountingEventListener(AccountingService accountingService) {
        this.accountingService = accountingService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void recordCustomerPayment(PaymentCompletedEvent event) {
        String fundAccount = event.method() == CustomerPaymentMethod.COD ? "111" : "112";
        List<AccountingService.PostingLine> lines = new ArrayList<>();
        lines.add(AccountingService.PostingLine.debit(fundAccount, "Thu tiền đơn hàng #" + event.orderId(), event.amount()));
        lines.add(AccountingService.PostingLine.credit("511", "Doanh thu đơn hàng #" + event.orderId(), event.amount()));
        if (event.costAmount() > 0) {
            lines.add(AccountingService.PostingLine.debit("632", "Giá vốn đơn hàng #" + event.orderId(), event.costAmount()));
            lines.add(AccountingService.PostingLine.credit("156", "Xuất kho cho đơn hàng #" + event.orderId(), event.costAmount()));
        }
        accountingService.postAutomatic(
                AccountingSourceType.CUSTOMER_PAYMENT,
                "PAYMENT-" + event.paymentId(),
                LocalDate.ofInstant(event.occurredAt(), BUSINESS_ZONE),
                "Ghi nhận thanh toán thành công cho đơn hàng #" + event.orderId(),
                lines
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void recordCustomerRefund(PaymentRefundedEvent event) {
        String fundAccount = event.method() == RefundMethod.COD_MANUAL ? "111" : "112";
        accountingService.postAutomatic(
                AccountingSourceType.CUSTOMER_REFUND,
                "REFUND-" + event.refundId(),
                LocalDate.ofInstant(event.occurredAt(), BUSINESS_ZONE),
                "Hoàn tiền cho đơn hàng #" + event.orderId(),
                List.of(
                        AccountingService.PostingLine.debit("521", "Giảm trừ doanh thu", event.amount()),
                        AccountingService.PostingLine.credit(fundAccount, "Tiền hoàn cho khách hàng", event.amount())
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void recordInventoryReceipt(ImportNoteCompletedEvent event) {
        double amount = Math.round(event.items().stream()
                .mapToDouble(item -> item.quantity() * item.importPrice())
                .sum() * 100.0) / 100.0;
        if (amount <= 0) return;
        accountingService.postAutomatic(
                AccountingSourceType.IMPORT_RECEIPT,
                "IMPORT-" + event.importNoteId(),
                LocalDate.ofInstant(event.occurredAt(), BUSINESS_ZONE),
                "Nhập hàng theo phiếu #" + event.importNoteId(),
                List.of(
                        AccountingService.PostingLine.debit("156", "Tăng giá trị hàng hóa", amount),
                        AccountingService.PostingLine.credit("331", "Tăng công nợ nhà cung cấp #" + event.supplierId(), amount)
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void recordSupplierPayment(SupplierPaymentRecordedEvent event) {
        String fundAccount = event.method() == SupplierPaymentMethod.CASH ? "111" : "112";
        accountingService.postAutomatic(
                AccountingSourceType.SUPPLIER_PAYMENT,
                "SUPPLIER-PAYMENT-" + event.transactionId(),
                event.paidDate(),
                "Thanh toán công nợ nhà cung cấp #" + event.supplierId(),
                List.of(
                        AccountingService.PostingLine.debit("331", "Giảm công nợ phiếu nhập #" + event.importNoteId(), event.amount()),
                        AccountingService.PostingLine.credit(fundAccount, "Chi tiền thanh toán nhà cung cấp", event.amount())
                )
        );
    }
}
