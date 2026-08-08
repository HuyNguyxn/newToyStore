package com.example.new_toy_store.customer_payment.domain;

import com.example.new_toy_store.global.common.BaseRootEntity;
import com.example.new_toy_store.customer_payment.domain.exception.InvalidCustomerCustomerPaymentDataException;
import com.example.new_toy_store.customer_payment.domain.exception.InvalidCustomerCustomerPaymentOperationException;
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
        name = "payment_transactions",
        indexes = {
                @Index(name = "idx_payment_order_id", columnList = "order_id"),
                @Index(name = "idx_payment_user_id", columnList = "user_id"),
                @Index(name = "idx_payment_status", columnList = "status"),
                @Index(name = "idx_payment_method", columnList = "method"),
                @Index(name = "idx_payment_idempotency_key", columnList = "idempotency_key"),
                @Index(name = "idx_payment_created_at", columnList = "created_at"),
                @Index(name = "idx_payment_user_status_created", columnList = "user_id, status, created_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_user_idempotency_key", columnNames = {"user_id", "idempotency_key"})
        }
)
public class CustomerPaymentTransaction extends BaseRootEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustomerPaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CustomerPaymentStatus status;

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

    @Column(name = "idempotency_key", length = 80)
    private String idempotencyKey;

    protected CustomerPaymentTransaction() {}

    public CustomerPaymentTransaction(Integer orderId, Integer userId, CustomerPaymentMethod method, double amount) {
        if (orderId == null) throw new InvalidCustomerCustomerPaymentDataException("orderId", "Order id must not be empty");
        if (userId == null) throw new InvalidCustomerCustomerPaymentDataException("userId", "User id must not be empty");
        if (method == null) throw InvalidCustomerCustomerPaymentDataException.emptyMethod();
        if (!method.isAvailable()) throw InvalidCustomerCustomerPaymentDataException.unsupportedMethod(method);
        if (amount <= 0) throw new InvalidCustomerCustomerPaymentDataException("amount", "Payment amount must be greater than 0");

        this.orderId = orderId;
        this.userId = userId;
        this.method = method;
        this.status = CustomerPaymentStatus.PENDING;
        this.amount = roundAmount(amount);
        this.expiredAt = LocalDateTime.now().plusMinutes(30);
    }

    public void attachIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = sanitize(idempotencyKey);
    }

    public void succeed(String providerTransactionId) {
        changeStatus(CustomerPaymentStatus.SUCCEEDED);
        this.providerTransactionId = sanitize(providerTransactionId);
        this.failureReason = null;
        this.cancelReason = null;
        this.paidAt = LocalDateTime.now();
    }

    public void collectCod(String collectionReference) {
        if (this.method != CustomerPaymentMethod.COD) {
            throw new InvalidCustomerCustomerPaymentOperationException("collectCod", "Only COD payments can be collected manually after delivery.");
        }
        succeed(collectionReference);
    }

    public void fail(String reason) {
        changeStatus(CustomerPaymentStatus.FAILED);
        this.failureReason = requireReason(reason, "Failure reason must not be empty");
    }

    public void cancel(String reason) {
        if (!this.status.canBeCancelled()) {
            throw InvalidCustomerCustomerPaymentOperationException.invalidTransition(this.status, CustomerPaymentStatus.CANCELLED, this.id);
        }
        changeStatus(CustomerPaymentStatus.CANCELLED);
        this.cancelReason = requireReason(reason, "Cancel reason must not be empty");
    }

    public void expire() {
        changeStatus(CustomerPaymentStatus.EXPIRED);
    }

    public void requestRefund() {
        changeStatus(CustomerPaymentStatus.REFUND_PENDING);
    }

    public void completeRefund() {
        changeStatus(CustomerPaymentStatus.REFUNDED);
    }

    public void failRefund() {
        changeStatus(CustomerPaymentStatus.REFUND_FAILED);
    }

    public void changeStatus(CustomerPaymentStatus nextStatus) {
        if (nextStatus == null) {
            throw InvalidCustomerCustomerPaymentDataException.emptyStatus();
        }
        if (!this.status.canTransitionTo(nextStatus)) {
            throw InvalidCustomerCustomerPaymentOperationException.invalidTransition(this.status, nextStatus, this.id);
        }
        this.status = nextStatus;
    }

    private static double roundAmount(double value) {
        double limitedValue = Math.min(value, 999_999_999.99);
        return Math.max(0.0, Math.round(limitedValue * 100.0) / 100.0);
    }

    private static String requireReason(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidCustomerCustomerPaymentDataException("reason", message);
        }
        return value.trim();
    }

    private static String sanitize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    public Integer getId() { return id; }
    public Integer getOrderId() { return orderId; }
    public Integer getUserId() { return userId; }
    public CustomerPaymentMethod getMethod() { return method; }
    public CustomerPaymentStatus getStatus() { return status; }
    public double getAmount() { return amount; }
    public String getProviderTransactionId() { return providerTransactionId; }
    public String getFailureReason() { return failureReason; }
    public String getCancelReason() { return cancelReason; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getExpiredAt() { return expiredAt; }
    public String getIdempotencyKey() { return idempotencyKey; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof CustomerPaymentTransaction other && id != null && id.equals(other.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
