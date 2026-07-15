package com.example.new_toy_store.customer_return.domain;

import com.example.new_toy_store.customer_return.domain.exception.InvalidCustomerReturnOperationException;
import com.example.new_toy_store.customer_return.domain.exception.InvalidCustomerReturnTransitionException;
import com.example.new_toy_store.global.common.BaseRootEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "customer_returns",
        indexes = {
                @Index(name = "idx_cust_return_order_id", columnList = "order_id"),
                @Index(name = "idx_cust_return_status", columnList = "status")
        }
)
public class CustomerReturn extends BaseRootEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "return_shipping_fee", nullable = false)
    private double returnShippingFee = 0.0;

    @Column(name = "admin_note", length = 500)
    private String adminNote;

    @Column(name = "deadline_for_extra_info")
    private LocalDateTime deadlineForExtraInfo;

    @Column(name = "is_high_risk", nullable = false)
    private boolean isHighRisk = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerReturnStatus status = CustomerReturnStatus.REQUESTED;

    @OneToMany(mappedBy = "customerReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerReturnItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "customerReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerReturnHistory> histories = new ArrayList<>();

    @OneToMany(mappedBy = "customerReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerReturnImage> proofImages = new ArrayList<>();

    protected CustomerReturn() {}

    public CustomerReturn(Integer orderId, List<CustomerReturnItem> newItems, List<String> imageUrls, String actionBy, String initialNote) {
        this.orderId = orderId;
        newItems.forEach(this::addItem);

        if (imageUrls != null) {
            imageUrls.forEach(this::addProofImage);
        }
        logHistory(null, this.status, actionBy, initialNote);
    }

    public void addItem(CustomerReturnItem item) {
        this.items.add(item);
        item.assignToReturn(this);
    }

    public void addProofImage(String imageUrl) {
        CustomerReturnImage image = new CustomerReturnImage(imageUrl);
        this.proofImages.add(image);
        image.assignToReturn(this);
    }

    private void logHistory(CustomerReturnStatus oldStatus, CustomerReturnStatus newStatus, String actionBy, String note) {
        CustomerReturnHistory history = new CustomerReturnHistory(oldStatus, newStatus, actionBy, note);
        this.histories.add(history);
        history.assignToReturn(this);
    }

    public void markAsHighRisk(String note) {
        this.isHighRisk = true;
        logHistory(this.status, this.status, "SYSTEM", note);
    }

    public double calculateRawTotalRefund() {
        double itemsTotal = items.stream().mapToDouble(CustomerReturnItem::getExpectedRefundAmount).sum();
        return Math.max(0, itemsTotal - returnShippingFee);
    }

    public void approveReturn(String actionBy, String note) {
        changeStatus(CustomerReturnStatus.APPROVED, actionBy, note);
        this.adminNote = null;
        this.deadlineForExtraInfo = null;
    }

    public void rejectReturn(String actionBy, String reason) {
        changeStatus(CustomerReturnStatus.REJECTED, actionBy, reason);
        this.adminNote = reason;
        this.deadlineForExtraInfo = null;
    }

    public void requireMoreInfo(String actionBy, String adminMessage, int expirationDays) {
        changeStatus(CustomerReturnStatus.NEEDS_MORE_INFO, actionBy, adminMessage);
        this.adminNote = adminMessage;
        this.deadlineForExtraInfo = LocalDateTime.now().plusDays(expirationDays);
    }

    public void updateInfoFromCustomer(String actionBy, String note) {
        changeStatus(CustomerReturnStatus.REQUESTED, actionBy, note);
        this.adminNote = null;
        this.deadlineForExtraInfo = null;
    }

    public void cancelByUser(String actionBy, String note) {
        changeStatus(CustomerReturnStatus.CANCELLED, actionBy, note);
    }

    public void receiveItems(String actionBy, String note) {
        changeStatus(CustomerReturnStatus.RECEIVED, actionBy, note);
    }

    public void passQualityControl(String actionBy, String qcNote) {
        changeStatus(CustomerReturnStatus.INSPECTED_OK, actionBy, qcNote);
    }

    public void failQualityControl(String actionBy, String failureReason) {
        changeStatus(CustomerReturnStatus.INSPECTED_FAILED, actionBy, failureReason);
        this.adminNote = failureReason;
    }

    public void openDispute(String actionBy, String disputeReason) {
        if (this.status != CustomerReturnStatus.REJECTED && this.status != CustomerReturnStatus.INSPECTED_FAILED) {
            throw InvalidCustomerReturnOperationException.invalidDisputeState(this.status.getDisplayName());
        }
        changeStatus(CustomerReturnStatus.DISPUTED, actionBy, disputeReason);
    }

    public void resolveDispute(String actionBy, boolean isApproved, String note) {
        if (this.status != CustomerReturnStatus.DISPUTED) {
            throw InvalidCustomerReturnOperationException.notInDisputeState(this.status.getDisplayName());
        }
        CustomerReturnStatus newStatus = isApproved ? CustomerReturnStatus.APPROVED : CustomerReturnStatus.REJECTED;
        changeStatus(newStatus, actionBy, note);
        this.adminNote = note;
    }

    public void finalizeRefund(String actionBy, String note) {
        changeStatus(CustomerReturnStatus.REFUNDED, actionBy, note);
    }

    public void finalizeReplace(String actionBy, String note) {
        changeStatus(CustomerReturnStatus.REPLACED, actionBy, note);
    }

    private void changeStatus(CustomerReturnStatus newStatus, String actionBy, String note) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw new InvalidCustomerReturnTransitionException(this.status.getDisplayName(), newStatus.getDisplayName());
        }
        logHistory(this.status, newStatus, actionBy, note);
        this.status = newStatus;
    }

    @Override
    public void delete() {
        super.delete();
        this.items.forEach(CustomerReturnItem::delete);
        this.histories.forEach(CustomerReturnHistory::delete);
        this.proofImages.forEach(CustomerReturnImage::delete);
    }

    public Integer getId() { return id; }
    public Integer getOrderId() { return orderId; }
    public double getReturnShippingFee() { return returnShippingFee; }
    public String getAdminNote() { return adminNote; }
    public LocalDateTime getDeadlineForExtraInfo() { return deadlineForExtraInfo; }
    public boolean isHighRisk() { return isHighRisk; }
    public CustomerReturnStatus getStatus() { return status; }
    public List<CustomerReturnItem> getItems() { return Collections.unmodifiableList(items); }
    public List<CustomerReturnHistory> getHistories() { return Collections.unmodifiableList(histories); }
    public List<CustomerReturnImage> getProofImages() { return Collections.unmodifiableList(proofImages); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof CustomerReturn p && id != null && id.equals(p.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}