package com.example.new_toy_store.payment.domain;

import com.example.new_toy_store.global.common.BaseRootEntity;
import com.example.new_toy_store.payment.domain.exception.InvalidPaymentDataException;
import com.example.new_toy_store.payment.domain.exception.InvalidPaymentOperationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "payment_refunds",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_refund_code", columnNames = "refund_code")
        },
        indexes = {
                @Index(name = "idx_refund_payment", columnList = "payment_id"),
                @Index(name = "idx_refund_order", columnList = "order_id"),
                @Index(name = "idx_refund_user", columnList = "user_id"),
                @Index(name = "idx_refund_status", columnList = "status"),
                @Index(name = "idx_refund_method", columnList = "method"),
                @Index(name = "idx_refund_created_at", columnList = "created_at")
        }
)
public class PaymentRefund extends BaseRootEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "payment_id", nullable = false)
    private Integer paymentId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "refund_code", nullable = false, length = 50)
    private String refundCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RefundMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RefundStatus status;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false, length = 255)
    private String reason;

    @Column(name = "provider_refund_id", length = 100)
    private String providerRefundId;

    @Column(name = "failed_reason", length = 255)
    private String failedReason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    protected PaymentRefund() {}

    public PaymentRefund(
            Integer paymentId,
            Integer orderId,
            Integer userId,
            String refundCode,
            RefundMethod method,
            double amount,
            String reason
    ) {
        if (paymentId == null) throw new InvalidPaymentDataException("paymentId", "Payment id must not be empty.");
        if (orderId == null) throw new InvalidPaymentDataException("orderId", "Order id must not be empty.");
        if (userId == null) throw new InvalidPaymentDataException("userId", "User id must not be empty.");
        if (refundCode == null || refundCode.trim().isEmpty()) {
            throw new InvalidPaymentDataException("refundCode", "Refund code must not be empty.");
        }
        if (method == null) throw new InvalidPaymentDataException("refundMethod", "Refund method must not be empty.");
        if (amount <= 0) throw new InvalidPaymentDataException("amount", "Refund amount must be greater than 0.");
        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidPaymentDataException("reason", "Refund reason must not be empty.");
        }

        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.refundCode = refundCode.trim();
        this.method = method;
        this.status = RefundStatus.PENDING;
        this.amount = roundAmount(amount);
        this.reason = reason.trim();
    }

    public void startProcessing() {
        changeStatus(RefundStatus.PROCESSING);
        this.processedAt = LocalDateTime.now();
    }

    public void succeed(String providerRefundId) {
        changeStatus(RefundStatus.SUCCEEDED);
        this.providerRefundId = sanitize(providerRefundId);
        this.failedReason = null;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        changeStatus(RefundStatus.FAILED);
        this.failedReason = requireReason(reason, "Failed reason must not be empty.");
    }

    public void reject(String reason) {
        changeStatus(RefundStatus.REJECTED);
        this.failedReason = requireReason(reason, "Reject reason must not be empty.");
    }

    public void cancel(String reason) {
        changeStatus(RefundStatus.CANCELLED);
        this.failedReason = requireReason(reason, "Cancel reason must not be empty.");
    }

    private void changeStatus(RefundStatus nextStatus) {
        if (!this.status.canTransitionTo(nextStatus)) {
            throw InvalidPaymentOperationException.invalidRefundTransition(this.id, this.status, nextStatus);
        }
        this.status = nextStatus;
    }

    private static String requireReason(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidPaymentDataException("reason", message);
        }
        return value.trim();
    }

    private static String sanitize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private static double roundAmount(double value) {
        double limitedValue = Math.min(value, 999_999_999.99);
        return Math.max(0.0, Math.round(limitedValue * 100.0) / 100.0);
    }

    public Integer getId() { return id; }
    public Integer getPaymentId() { return paymentId; }
    public Integer getOrderId() { return orderId; }
    public Integer getUserId() { return userId; }
    public String getRefundCode() { return refundCode; }
    public RefundMethod getMethod() { return method; }
    public RefundStatus getStatus() { return status; }
    public double getAmount() { return amount; }
    public String getReason() { return reason; }
    public String getProviderRefundId() { return providerRefundId; }
    public String getFailedReason() { return failedReason; }
    public LocalDateTime getProcessedAt() { return processedAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof PaymentRefund other && id != null && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
