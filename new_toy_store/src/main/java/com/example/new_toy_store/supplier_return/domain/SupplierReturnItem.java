package com.example.new_toy_store.supplier_return.domain;

import com.example.new_toy_store.global.common.BaseSoftDeleteEntity;
import com.example.new_toy_store.supplier_return.domain.exception.InvalidSupplierReturnOperationException;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "supplier_return_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_sup_ret_variant", columnNames = {"supplier_return_id", "variant_id"})
        },
        indexes = {
                @Index(name = "idx_sup_ret_item_variant", columnList = "variant_id")
        }
)
public class SupplierReturnItem extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_return_id", nullable = false)
    private SupplierReturn supplierReturn;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "variant_id", nullable = false)
    private Integer variantId;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "accepted_quantity", nullable = false)
    private int acceptedQuantity;

    @Column(name = "return_price", nullable = false)
    private double returnPrice;

    @Column(name = "discount_amount", nullable = false)
    private double discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_code", nullable = false)
    private SupplierReturnReason reasonCode;

    @Column(name = "discrepancy_reason", length = 500)
    private String discrepancyReason;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    protected SupplierReturnItem() {
    }

    public SupplierReturnItem(
            Integer productId,
            Integer variantId,
            String productName,
            int quantity,
            double returnPrice,
            double discountAmount,
            SupplierReturnReason reasonCode,
            String batchNumber,
            LocalDate expiryDate) {

        if (productId == null || variantId == null) {
            throw InvalidSupplierReturnOperationException.emptyField("Mã sản phẩm/biến thể");
        }
        if (quantity <= 0) {
            throw InvalidSupplierReturnOperationException.invalidQuantity();
        }
        if (returnPrice < 0 || discountAmount < 0) {
            throw InvalidSupplierReturnOperationException.negativeFinancialValue();
        }
        if (reasonCode == null) {
            throw InvalidSupplierReturnOperationException.emptyField("Lý do trả hàng");
        }
        if (batchNumber == null || batchNumber.trim().isEmpty()) {
            throw InvalidSupplierReturnOperationException.emptyField("Mã lô hàng (Batch Number)");
        }
        if (expiryDate == null) {
            throw InvalidSupplierReturnOperationException.emptyField("Hạn sử dụng lô");
        }

        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;

        this.quantity = quantity;
        this.acceptedQuantity = quantity;

        this.returnPrice = returnPrice;
        this.discountAmount = discountAmount;
        this.reasonCode = reasonCode;
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
    }

    public void updateInspection(int acceptedQuantity, String discrepancyReason) {
        if (acceptedQuantity < 0 || acceptedQuantity > this.quantity) {
            throw InvalidSupplierReturnOperationException.invalidAcceptedQuantity(this.quantity);
        }
        this.acceptedQuantity = acceptedQuantity;
        this.discrepancyReason = discrepancyReason;
    }

    void assignToReturn(SupplierReturn supplierReturn) {
        this.supplierReturn = supplierReturn;
    }

    public Integer getId() {
        return id;
    }

    public Integer getProductId() {
        return productId;
    }

    public Integer getVariantId() {
        return variantId;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getAcceptedQuantity() {
        return acceptedQuantity;
    }

    public double getReturnPrice() {
        return returnPrice;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public SupplierReturnReason getReasonCode() {
        return reasonCode;
    }

    public String getDiscrepancyReason() {
        return discrepancyReason;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof SupplierReturnItem i && id != null && id.equals(i.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}