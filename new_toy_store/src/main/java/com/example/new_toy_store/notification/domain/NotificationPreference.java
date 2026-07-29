package com.example.new_toy_store.notification.domain;

import com.example.new_toy_store.global.common.BaseRootEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "notification_preferences",
        indexes = @Index(name = "idx_notification_preference_user", columnList = "user_id"),
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_preference_user", columnNames = "user_id")
)
public class NotificationPreference extends BaseRootEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled = true;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = false;

    @Column(name = "order_enabled", nullable = false)
    private boolean orderEnabled = true;

    @Column(name = "payment_enabled", nullable = false)
    private boolean paymentEnabled = true;

    @Column(name = "shipment_enabled", nullable = false)
    private boolean shipmentEnabled = true;

    @Column(name = "return_enabled", nullable = false)
    private boolean returnEnabled = true;

    @Column(name = "review_enabled", nullable = false)
    private boolean reviewEnabled = true;

    @Column(name = "cart_enabled", nullable = false)
    private boolean cartEnabled = true;

    @Column(name = "system_enabled", nullable = false)
    private boolean systemEnabled = true;

    protected NotificationPreference() {
    }

    public NotificationPreference(Integer userId) {
        this.userId = userId;
    }

    public void update(
            boolean inAppEnabled,
            boolean emailEnabled,
            boolean orderEnabled,
            boolean paymentEnabled,
            boolean shipmentEnabled,
            boolean returnEnabled,
            boolean reviewEnabled,
            boolean cartEnabled,
            boolean systemEnabled
    ) {
        this.inAppEnabled = inAppEnabled;
        this.emailEnabled = emailEnabled;
        this.orderEnabled = orderEnabled;
        this.paymentEnabled = paymentEnabled;
        this.shipmentEnabled = shipmentEnabled;
        this.returnEnabled = returnEnabled;
        this.reviewEnabled = reviewEnabled;
        this.cartEnabled = cartEnabled;
        this.systemEnabled = systemEnabled;
    }

    public boolean allowsInApp(NotificationType type) {
        return inAppEnabled && allowsCategory(type.getCategory());
    }

    public boolean allowsEmail(NotificationType type) {
        return emailEnabled && type.isEmailSupported() && allowsCategory(type.getCategory());
    }

    private boolean allowsCategory(NotificationCategory category) {
        return switch (category) {
            case ORDER -> orderEnabled;
            case PAYMENT -> paymentEnabled;
            case SHIPMENT -> shipmentEnabled;
            case RETURN -> returnEnabled;
            case REVIEW -> reviewEnabled;
            case CART -> cartEnabled;
            case SYSTEM -> systemEnabled;
        };
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public boolean isInAppEnabled() { return inAppEnabled; }
    public boolean isEmailEnabled() { return emailEnabled; }
    public boolean isOrderEnabled() { return orderEnabled; }
    public boolean isPaymentEnabled() { return paymentEnabled; }
    public boolean isShipmentEnabled() { return shipmentEnabled; }
    public boolean isReturnEnabled() { return returnEnabled; }
    public boolean isReviewEnabled() { return reviewEnabled; }
    public boolean isCartEnabled() { return cartEnabled; }
    public boolean isSystemEnabled() { return systemEnabled; }
}
