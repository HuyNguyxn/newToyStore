package com.example.new_toy_store.supplier_payment.domain;

import com.example.new_toy_store.global.common.BaseRootEntity;
import com.example.new_toy_store.supplier_payment.domain.exception.InvalidSupplierPaymentOperationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "supplier_payment_transactions",
        indexes = {
                @Index(name = "idx_supplier_payment_tx_invoice", columnList = "invoice_id"),
                @Index(name = "idx_supplier_payment_tx_paid_date", columnList = "paid_date")
        }
)
public class SupplierPaymentTransaction extends BaseRootEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private SupplierPaymentInvoice invoice;

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SupplierPaymentMethod method;

    @Column(name = "reference_code", length = 100)
    private String referenceCode;

    @Column(name = "paid_date", nullable = false)
    private LocalDate paidDate;

    @Column(length = 500)
    private String note;

    protected SupplierPaymentTransaction() {
    }

    public SupplierPaymentTransaction(double amount, SupplierPaymentMethod method, String referenceCode, LocalDate paidDate, String note) {
        if (amount <= 0) {
            throw InvalidSupplierPaymentOperationException.invalidAmount(amount);
        }
        if (method == null) {
            throw InvalidSupplierPaymentOperationException.emptyField("Phương thức thanh toán");
        }
        this.amount = Math.round(amount * 100.0) / 100.0;
        this.method = method;
        this.referenceCode = referenceCode;
        this.paidDate = paidDate == null ? LocalDate.now() : paidDate;
        this.note = note;
    }

    public void assignToInvoice(SupplierPaymentInvoice invoice) {
        this.invoice = invoice;
    }

    public Integer getId() { return id; }
    public SupplierPaymentInvoice getInvoice() { return invoice; }
    public double getAmount() { return amount; }
    public SupplierPaymentMethod getMethod() { return method; }
    public String getReferenceCode() { return referenceCode; }
    public LocalDate getPaidDate() { return paidDate; }
    public String getNote() { return note; }
}
