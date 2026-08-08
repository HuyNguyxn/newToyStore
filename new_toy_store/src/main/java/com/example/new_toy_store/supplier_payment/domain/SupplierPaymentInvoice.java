package com.example.new_toy_store.supplier_payment.domain;

import com.example.new_toy_store.global.common.BaseRootEntity;
import com.example.new_toy_store.supplier_payment.domain.exception.InvalidSupplierPaymentOperationException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "supplier_payment_invoices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_supplier_payment_import_note", columnNames = "import_note_id")
        },
        indexes = {
                @Index(name = "idx_supplier_payment_supplier", columnList = "supplier_id"),
                @Index(name = "idx_supplier_payment_status", columnList = "status"),
                @Index(name = "idx_supplier_payment_due_date", columnList = "due_date")
        }
)
public class SupplierPaymentInvoice extends BaseRootEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "supplier_id", nullable = false)
    private Integer supplierId;

    @Column(name = "import_note_id", nullable = false)
    private Integer importNoteId;

    @Column(name = "invoice_code", nullable = false, unique = true, length = 50)
    private String invoiceCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SupplierPaymentStatus status = SupplierPaymentStatus.PENDING;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;

    @Column(name = "paid_amount", nullable = false)
    private double paidAmount = 0.0;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(length = 500)
    private String note;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierPaymentTransaction> transactions = new ArrayList<>();

    protected SupplierPaymentInvoice() {
    }

    public SupplierPaymentInvoice(Integer supplierId, Integer importNoteId, String invoiceCode, double totalAmount, LocalDate dueDate, String note) {
        if (supplierId == null) {
            throw InvalidSupplierPaymentOperationException.emptyField("Mã nhà cung cấp");
        }
        if (importNoteId == null) {
            throw InvalidSupplierPaymentOperationException.emptyField("Mã phiếu nhập");
        }
        if (invoiceCode == null || invoiceCode.isBlank()) {
            throw InvalidSupplierPaymentOperationException.emptyField("Mã công nợ");
        }
        if (totalAmount <= 0) {
            throw InvalidSupplierPaymentOperationException.invalidAmount(totalAmount);
        }

        this.supplierId = supplierId;
        this.importNoteId = importNoteId;
        this.invoiceCode = invoiceCode.trim();
        this.totalAmount = roundMoney(totalAmount);
        this.dueDate = dueDate;
        this.note = note;
    }

    public void recordPayment(double amount, SupplierPaymentMethod method, String referenceCode, LocalDate paidDate, String note) {
        if (status.isClosed()) {
            throw InvalidSupplierPaymentOperationException.closedInvoice(id, status);
        }
        if (amount <= 0) {
            throw InvalidSupplierPaymentOperationException.invalidAmount(amount);
        }
        if (amount > getRemainingAmount()) {
            throw InvalidSupplierPaymentOperationException.paymentExceedsRemaining(id, amount, getRemainingAmount());
        }

        SupplierPaymentTransaction transaction = new SupplierPaymentTransaction(amount, method, referenceCode, paidDate, note);
        transaction.assignToInvoice(this);
        transactions.add(transaction);
        paidAmount = roundMoney(paidAmount + amount);
        refreshStatusAfterPayment();
    }

    public void markOverdue(LocalDate today) {
        if (!status.isClosed() && dueDate != null && today.isAfter(dueDate) && status.canTransitionTo(SupplierPaymentStatus.OVERDUE)) {
            status = SupplierPaymentStatus.OVERDUE;
        }
    }

    public void cancel(String reason) {
        if (!status.canTransitionTo(SupplierPaymentStatus.CANCELLED)) {
            throw InvalidSupplierPaymentOperationException.invalidTransition(status, SupplierPaymentStatus.CANCELLED);
        }
        this.status = SupplierPaymentStatus.CANCELLED;
        this.note = reason == null || reason.isBlank() ? this.note : reason;
    }

    private void refreshStatusAfterPayment() {
        SupplierPaymentStatus nextStatus = paidAmount >= totalAmount
                ? SupplierPaymentStatus.PAID
                : SupplierPaymentStatus.PARTIALLY_PAID;
        if (!status.canTransitionTo(nextStatus) && status != nextStatus) {
            throw InvalidSupplierPaymentOperationException.invalidTransition(status, nextStatus);
        }
        status = nextStatus;
    }

    private double roundMoney(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public Integer getId() { return id; }
    public Integer getSupplierId() { return supplierId; }
    public Integer getImportNoteId() { return importNoteId; }
    public String getInvoiceCode() { return invoiceCode; }
    public SupplierPaymentStatus getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public double getPaidAmount() { return paidAmount; }
    public double getRemainingAmount() { return roundMoney(Math.max(0.0, totalAmount - paidAmount)); }
    public LocalDate getDueDate() { return dueDate; }
    public String getNote() { return note; }
    public List<SupplierPaymentTransaction> getTransactions() { return Collections.unmodifiableList(transactions); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof SupplierPaymentInvoice invoice && id != null && id.equals(invoice.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
