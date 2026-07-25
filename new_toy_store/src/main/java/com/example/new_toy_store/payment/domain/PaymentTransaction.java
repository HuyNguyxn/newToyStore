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
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "payment_transactions",
        indexes = {
                @Index(name = "idx_payment_order_id", columnList = "order_id"),
                @Index(name = "idx_payment_user_id", columnList = "user_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_method", columnList = "method"),
                @Index(name = "idx_payment_created_at", columnList = "created_at"),
                @Index(name = "idx_payment_user_status_created", columnList = "user_id, status, created_at")
        }
)
public class PaymentTransaction extends BaseRootEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(nullable = false)
    private double amount;

    @Column(name = "provider_transaction_id", length = 100)
    private String providerTransactionId;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    protected PaymentTransaction() {}

    public PaymentTransaction(Integer orderId, Integer userId, PaymentMethod method, double amount) {
        if (orderId == null) throw new InvalidPaymentDataException("orderId", "Order id must not be empty");
        if (userId == null) throw new InvalidPaymentDataException("userId", "User id must not be empty");
        if (method == null) throw InvalidPaymentDataException.emptyMethod();
        if (!method.isAvailable()) throw InvalidPaymentDataException.unsupportedMethod(method);
        if (amount <= 0) throw new InvalidPaymentDataException("amount", "Payment amount must be greater than 0");

        this.orderId = orderId;
        this.userId = userId;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.amount = roundAmount(amount);
        this.expiredAt = LocalDateTime.now().plusMinutes(30);
    }

    public void succeed(String providerTransactionId) {
        changeStatus(PaymentStatus.SUCCEEDED);
        this.providerTransactionId = sanitize(providerTransactionId);
        this.failureReason = null;
        this.cancelReason = null;
        this.paidAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        changeStatus(PaymentStatus.FAILED);
        this.failureReason = requireReason(reason, "Failure reason must not be empty");
    }

    public void cancel(String reason) {
        if (!this.status.canBeCancelled()) {
            throw InvalidPaymentOperationException.invalidTransition(this.status, PaymentStatus.CANCELLED, this.id);
        }
        changeStatus(PaymentStatus.CANCELLED);
        this.cancelReason = requireReason(reason, "Cancel reason must not be empty");
    }

    public void expire() {
        changeStatus(PaymentStatus.EXPIRED);
    }

    public void changeStatus(PaymentStatus nextStatus) {
        if (nextStatus == null) {
            throw InvalidPaymentDataException.emptyStatus();
        }
        if (!this.status.canTransitionTo(nextStatus)) {
            throw InvalidPaymentOperationException.invalidTransition(this.status, nextStatus, this.id);
        }
        this.status = nextStatus;
    }

    private static double roundAmount(double value) {
        double limitedValue = Math.min(value, 999_999_999.99);
        return Math.max(0.0, Math.round(limitedValue * 100.0) / 100.0);
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

    public Integer getId() { return id; }
    public Integer getOrderId() { return orderId; }
    public Integer getUserId() { return userId; }
    public PaymentMethod getMethod() { return method; }
    public PaymentStatus getStatus() { return status; }
    public double getAmount() { return amount; }
    public String getProviderTransactionId() { return providerTransactionId; }
    public String getFailureReason() { return failureReason; }
    public String getCancelReason() { return cancelReason; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getExpiredAt() { return expiredAt; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof PaymentTransaction other && id != null && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
