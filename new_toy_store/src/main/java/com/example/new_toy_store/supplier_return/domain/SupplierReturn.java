package com.example.new_toy_store.supplier_return.domain;

import com.example.new_toy_store.global.common.BaseRootEntity;
import com.example.new_toy_store.supplier_return.domain.exception.InvalidSupplierReturnOperationException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "supplier_returns",
        indexes = {
                @Index(name = "idx_sup_ret_supplier", columnList = "supplier_id"),
                @Index(name = "idx_sup_ret_status", columnList = "status"),
                @Index(name = "idx_sup_ret_import", columnList = "import_note_id")
        }
)
public class SupplierReturn extends BaseRootEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "supplier_id", nullable = false)
    private Integer supplierId;

    @Column(name = "import_note_id")
    private Integer importNoteId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierReturnStatus status = SupplierReturnStatus.DRAFT;

    @Column(name = "freight_cost", nullable = false)
    private double freightCost = 0.0;

    @Column(name = "restocking_fee", nullable = false)
    private double restockingFee = 0.0;

    @Column(name = "total_refund_amount", nullable = false)
    private double totalRefundAmount = 0.0;

    @Column(length = 500)
    private String note;

    @OneToMany(mappedBy = "supplierReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierReturnItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "supplierReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierReturnHistory> histories = new ArrayList<>();

    @OneToMany(mappedBy = "supplierReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SupplierReturnImage> images = new ArrayList<>();

    protected SupplierReturn() {
    }

    public SupplierReturn(
            Integer supplierId,
            Integer importNoteId,
            double freightCost,
            double restockingFee,
            String note,
            String actionBy) {

        if (supplierId == null) {
            throw InvalidSupplierReturnOperationException.emptyField("Mã nhà cung cấp");
        }

        this.supplierId = supplierId;
        this.importNoteId = importNoteId;
        this.note = note;

        updateFinancials(freightCost, restockingFee);
        logHistory(null, this.status, actionBy, "Khởi tạo phiếu trả hàng nháp");
    }

    public void addItem(SupplierReturnItem item) {
        checkReadOnly("Thêm sản phẩm");
        this.items.add(item);
        item.assignToReturn(this);
        recalculateTotalAmount();
    }

    public void addImage(SupplierReturnImage image) {
        this.images.add(image);
        image.assignToReturn(this);
    }

    public void updateFinancials(double freightCost, double restockingFee) {
        checkReadOnly("Cập nhật chi phí");

        if (freightCost < 0 || restockingFee < 0) {
            throw InvalidSupplierReturnOperationException.negativeFinancialValue();
        }

        this.freightCost = freightCost;
        this.restockingFee = restockingFee;
        recalculateTotalAmount();
    }

    private void recalculateTotalAmount() {
        double itemsRawTotal = items.stream().mapToDouble(item ->
                (item.getQuantity() * item.getReturnPrice()) + item.getTaxAmount() - item.getDiscountAmount()
        ).sum();

        double rawFinal = itemsRawTotal - this.freightCost - this.restockingFee;
        this.totalRefundAmount = Math.max(0.0, Math.round(rawFinal * 100.0) / 100.0);
    }

    public void submitForApproval(String actionBy, String note) {
        if (this.items.isEmpty()) {
            throw InvalidSupplierReturnOperationException.emptyItems();
        }
        changeStatus(SupplierReturnStatus.PENDING_APPROVAL, actionBy, note);
    }

    public void approve(String actionBy, String note) {
        changeStatus(SupplierReturnStatus.APPROVED, actionBy, note);
    }

    public void reject(String actionBy, String note) {
        changeStatus(SupplierReturnStatus.REJECTED, actionBy, note);
    }

    public void ship(String actionBy, String note) {
        changeStatus(SupplierReturnStatus.SHIPPED, actionBy, note);
    }

    public void complete(String actionBy, String note) {
        changeStatus(SupplierReturnStatus.COMPLETED, actionBy, note);
    }

    public void cancel(String actionBy, String note) {
        changeStatus(SupplierReturnStatus.CANCELLED, actionBy, note);
    }

    private void changeStatus(SupplierReturnStatus newStatus, String actionBy, String note) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw InvalidSupplierReturnOperationException.invalidTransition(
                    this.status.getDisplayName(),
                    newStatus.getDisplayName()
            );
        }
        logHistory(this.status, newStatus, actionBy, note);
        this.status = newStatus;
    }

    private void logHistory(SupplierReturnStatus oldStatus, SupplierReturnStatus newStatus, String actionBy, String note) {
        SupplierReturnHistory history = new SupplierReturnHistory(oldStatus, newStatus, actionBy, note);
        this.histories.add(history);
        history.assignToReturn(this);
    }

    private void checkReadOnly(String action) {
        if (this.status.isReadOnly()) {
            throw InvalidSupplierReturnOperationException.readOnlyState(action, this.status.getDisplayName());
        }
    }

    @Override
    public void delete() {
        super.delete();
        this.items.forEach(SupplierReturnItem::delete);
        this.histories.forEach(SupplierReturnHistory::delete);
        this.images.forEach(SupplierReturnImage::delete);
    }

    public Integer getId() {
        return id;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public Integer getImportNoteId() {
        return importNoteId;
    }

    public SupplierReturnStatus getStatus() {
        return status;
    }

    public double getFreightCost() {
        return freightCost;
    }

    public double getRestockingFee() {
        return restockingFee;
    }

    public double getTotalRefundAmount() {
        return totalRefundAmount;
    }

    public String getNote() {
        return note;
    }

    public List<SupplierReturnItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public List<SupplierReturnHistory> getHistories() {
        return Collections.unmodifiableList(histories);
    }

    public List<SupplierReturnImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof SupplierReturn p && id != null && id.equals(p.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}