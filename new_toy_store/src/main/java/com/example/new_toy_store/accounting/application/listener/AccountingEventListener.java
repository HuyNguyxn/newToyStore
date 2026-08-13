package com.example.new_toy_store.accounting.application.listener;

import com.example.new_toy_store.accounting.application.AccountingService;
import com.example.new_toy_store.accounting.domain.AccountingSourceType;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentMethod;
import com.example.new_toy_store.customer_payment.domain.RefundMethod;
import com.example.new_toy_store.global.event.ImportNoteCompletedEvent;
import com.example.new_toy_store.global.event.CustomerReturnStockRestorationRequestedEvent;
import com.example.new_toy_store.global.event.OrderStatusChangedEvent;
import com.example.new_toy_store.global.event.PaymentCompletedEvent;
import com.example.new_toy_store.global.event.PaymentRefundedEvent;
import com.example.new_toy_store.global.event.SupplierPaymentRecordedEvent;
import com.example.new_toy_store.global.event.SupplierReturnCompletedEvent;
import com.example.new_toy_store.global.event.SupplierReturnStockRestorationRequestedEvent;
import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentMethod;
import com.example.new_toy_store.order.application.facade.OrderFacade;
import com.example.new_toy_store.order.domain.OrderStatus;
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
    private final OrderFacade orderFacade;

    public AccountingEventListener(AccountingService accountingService, OrderFacade orderFacade) {
        this.accountingService = accountingService;
        this.orderFacade = orderFacade;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void recordCustomerPayment(PaymentCompletedEvent event) {
        String fundAccount = event.method() == CustomerPaymentMethod.COD ? "111" : "112";
        List<AccountingService.PostingLine> lines = new ArrayList<>();
        lines.add(AccountingService.PostingLine.debit(fundAccount, "Thu tiền đơn hàng #" + event.orderId(), event.amount()));
        lines.add(AccountingService.PostingLine.credit("3388", "Tiền khách trả trước cho đơn hàng #" + event.orderId(), event.amount()));
        accountingService.postAutomatic(
                AccountingSourceType.CUSTOMER_PAYMENT,
                "PAYMENT-" + event.paymentId(),
                LocalDate.ofInstant(event.occurredAt(), BUSINESS_ZONE),
                "Ghi nhận thanh toán thành công cho đơn hàng #" + event.orderId(),
                lines
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void recognizeCompletedOrder(OrderStatusChangedEvent event) {
        if (event.currentStatus() != OrderStatus.COMPLETED) return;
        var snapshot = orderFacade.getPaymentSnapshot(event.orderId());
        double revenueAmount = snapshot.getPayableAmount();
        double costAmount = snapshot.getCostAmount();
        List<AccountingService.PostingLine> lines = new ArrayList<>();
        if (revenueAmount > 0) {
            lines.add(AccountingService.PostingLine.debit("3388", "Kết chuyển tiền khách trả trước", revenueAmount));
            lines.add(AccountingService.PostingLine.credit("511", "Doanh thu đơn hàng #" + event.orderId(), revenueAmount));
        }
        if (costAmount > 0) {
            lines.add(AccountingService.PostingLine.debit("632", "Giá vốn đơn hàng #" + event.orderId(), costAmount));
            lines.add(AccountingService.PostingLine.credit("156", "Xuất kho cho đơn hàng #" + event.orderId(), costAmount));
        }
        if (lines.isEmpty()) return;
        accountingService.postAutomatic(
                AccountingSourceType.ORDER_COMPLETION,
                "ORDER-COMPLETION-" + event.orderId(),
                LocalDate.ofInstant(event.occurredAt(), BUSINESS_ZONE),
                "Ghi nhận giá vốn khi hoàn tất đơn hàng #" + event.orderId(),
                lines
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void recordReturnedSellableStock(CustomerReturnStockRestorationRequestedEvent event) {
        if (event.restoredCostAmount() <= 0) return;
        accountingService.postAutomatic(
                AccountingSourceType.CUSTOMER_RETURN_STOCK,
                "RETURN-STOCK-" + event.returnId(),
                LocalDate.ofInstant(event.occurredAt(), BUSINESS_ZONE),
                "Nhập lại hàng bán bị trả từ phiếu #" + event.returnId(),
                List.of(
                        AccountingService.PostingLine.debit("156", "Tăng hàng hóa có thể bán lại", event.restoredCostAmount()),
                        AccountingService.PostingLine.credit("632", "Giảm giá vốn hàng bán", event.restoredCostAmount())
                )
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void recordCustomerRefund(PaymentRefundedEvent event) {
        String fundAccount = event.method() == RefundMethod.COD_MANUAL ? "111" : "112";
        OrderStatus orderStatus = orderFacade.getPaymentSnapshot(event.orderId()).getStatus();
        String debitAccount = orderStatus == OrderStatus.CANCELLED ? "3388" : "521";
        String debitDescription = orderStatus == OrderStatus.CANCELLED
                ? "Hoàn tiền khách trả trước do hủy đơn"
                : "Giảm trừ doanh thu";
        accountingService.postAutomatic(
                AccountingSourceType.CUSTOMER_REFUND,
                "REFUND-" + event.refundId(),
                LocalDate.ofInstant(event.occurredAt(), BUSINESS_ZONE),
                "Hoàn tiền cho đơn hàng #" + event.orderId(),
                List.of(
                        AccountingService.PostingLine.debit(debitAccount, debitDescription, event.amount()),
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

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void recordSupplierReturnShipment(SupplierReturnCompletedEvent event) {
        double inventoryAmount = roundMoney(event.getInventoryAmount());
        if (inventoryAmount <= 0) return;

        double payableReduction = Math.min(inventoryAmount, roundMoney(event.getRefundAmount()));
        double returnCosts = roundMoney(inventoryAmount - payableReduction);
        List<AccountingService.PostingLine> lines = new ArrayList<>();
        if (payableReduction > 0) {
            lines.add(AccountingService.PostingLine.debit(
                    "331",
                    "Giảm công nợ nhà cung cấp #" + event.getSupplierId(),
                    payableReduction
            ));
        }
        if (returnCosts > 0) {
            lines.add(AccountingService.PostingLine.debit(
                    "642",
                    "Chi phí trả hàng nhà cung cấp",
                    returnCosts
            ));
        }
        lines.add(AccountingService.PostingLine.credit(
                "156",
                "Xuất kho trả nhà cung cấp #" + event.getSupplierId(),
                inventoryAmount
        ));

        accountingService.postAutomatic(
                AccountingSourceType.SUPPLIER_RETURN_SHIPMENT,
                "SUPPLIER-RETURN-" + event.getReturnId(),
                LocalDate.ofInstant(event.getOccurredAt(), BUSINESS_ZONE),
                "Xuất kho theo phiếu trả nhà cung cấp #" + event.getReturnId(),
                lines
        );
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void reverseSupplierReturnShipment(SupplierReturnStockRestorationRequestedEvent event) {
        double inventoryAmount = roundMoney(event.inventoryAmount());
        if (inventoryAmount <= 0) return;

        double payableRestoration = Math.min(inventoryAmount, roundMoney(event.refundAmount()));
        double returnCostReversal = roundMoney(inventoryAmount - payableRestoration);
        List<AccountingService.PostingLine> lines = new ArrayList<>();
        lines.add(AccountingService.PostingLine.debit(
                "156", "Nhập lại hàng trả nhà cung cấp bị hoàn về", inventoryAmount));
        if (payableRestoration > 0) {
            lines.add(AccountingService.PostingLine.credit(
                    "331", "Khôi phục công nợ nhà cung cấp #" + event.supplierId(), payableRestoration));
        }
        if (returnCostReversal > 0) {
            lines.add(AccountingService.PostingLine.credit(
                    "642", "Hoàn nhập chi phí trả hàng nhà cung cấp", returnCostReversal));
        }
        accountingService.postAutomatic(
                AccountingSourceType.SUPPLIER_RETURN_RESTORATION,
                "SUPPLIER-RETURN-RESTORE-" + event.returnId(),
                LocalDate.ofInstant(event.occurredAt(), BUSINESS_ZONE),
                "Hoàn nguyên phiếu trả nhà cung cấp #" + event.returnId() + " do hàng quay lại kho",
                lines
        );
    }

    private double roundMoney(double amount) {
        return Math.max(0.0, Math.round(amount * 100.0) / 100.0);
    }
}
