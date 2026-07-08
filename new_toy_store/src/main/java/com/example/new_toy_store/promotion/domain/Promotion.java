package com.example.new_toy_store.promotion.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import com.example.new_toy_store.promotion.domain.exception.InvalidPromotionOperationException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "promotions",
        indexes = {
                @Index(name = "idx_promo_code", columnList = "code", unique = true),
                @Index(name = "idx_promo_time_status", columnList = "is_active, start_date, end_date"),
                @Index(name = "idx_promo_scope_target", columnList = "scope, target_product_id")
        }
)
public class Promotion extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromotionScope scope;

    @Column(nullable = false)
    private double discountValue;

    @Column(name = "max_discount_amount")
    private Double maxDiscountAmount;

    @Column(name = "min_order_value")
    private Double minOrderValue;

    @Column(name = "target_product_id")
    private Integer targetProductId;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    private Integer usedCount = 0;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    protected Promotion() {}

    public Promotion(String code, String name, PromotionType type, PromotionScope scope, double discountValue, LocalDateTime startDate, LocalDateTime endDate) {
        if (code == null || code.trim().isEmpty()) {
            throw InvalidPromotionOperationException.nullPromoCode();
        }
        if (name == null || name.trim().isEmpty()) {
            throw InvalidPromotionOperationException.nullPromoName();
        }
        if (type == null || scope == null) {
            throw InvalidPromotionOperationException.nullPromoTypeOrScope();
        }

        this.code = code.toUpperCase().trim();
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.discountValue = Math.max(0.0, discountValue);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @PrePersist
    @PreUpdate
    private void validateEntityState() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw InvalidPromotionOperationException.invalidDateRange();
        }
        if (discountValue < 0) {
            throw InvalidPromotionOperationException.negativeDiscountValue(discountValue);
        }
        if (type == PromotionType.PERCENTAGE && discountValue > 100) {
            throw InvalidPromotionOperationException.percentageExceeded(discountValue);
        }
        if (usedCount < 0) {
            throw InvalidPromotionOperationException.negativeUsedCount(usedCount);
        }
    }

    public void setupConditions(Double minOrderValue, Double maxDiscountAmount, Integer targetProductId, Integer usageLimit) {
        this.scope.validateSetup(minOrderValue, targetProductId);
        if (usageLimit != null && usageLimit < this.usedCount) {
            throw InvalidPromotionOperationException.invalidUsageLimit(this.usedCount);
        }
        this.minOrderValue = minOrderValue != null ? Math.max(0.0, minOrderValue) : null;
        this.maxDiscountAmount = maxDiscountAmount != null ? Math.max(0.0, maxDiscountAmount) : null;
        this.targetProductId = targetProductId;
        this.usageLimit = usageLimit != null ? Math.max(1, usageLimit) : null;
    }

    public void updateDetails(String name, double discountValue, LocalDateTime startDate, LocalDateTime endDate, Double minOrderValue, Double maxDiscountAmount, Integer targetProductId, Integer usageLimit) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        this.discountValue = Math.max(0.0, discountValue);
        this.startDate = startDate;
        this.endDate = endDate;
        setupConditions(minOrderValue, maxDiscountAmount, targetProductId, usageLimit);
    }

    public boolean hasAvailableUsages() {
        return this.usageLimit == null || this.usedCount < this.usageLimit;
    }

    public boolean isCurrentlyValid() {
        if (!this.isActive || !hasAvailableUsages()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isStarted = (this.startDate == null || !now.isBefore(this.startDate));
        boolean isNotExpired = (this.endDate == null || !now.isAfter(this.endDate));

        return isStarted && isNotExpired;
    }

    public boolean isApplicableForOrder(double currentOrderTotal) {
        if (!isCurrentlyValid()) {
            return false;
        }
        return this.minOrderValue == null || currentOrderTotal >= this.minOrderValue;
    }

    public double applyDiscount(double originalAmount) {
        if (!isCurrentlyValid() || originalAmount <= 0) {
            return 0.0;
        }
        return this.type.calculateDiscount(originalAmount, this.discountValue, this.maxDiscountAmount);
    }

    public void incrementUsedCount() {
        if (!isCurrentlyValid()) {
            throw InvalidPromotionOperationException.quotaExceeded();
        }
        this.usedCount++;
    }

    public void decrementUsedCount() {
        if (this.usedCount <= 0) {
            throw InvalidPromotionOperationException.quotaZero();
        }
        this.usedCount--;
    }

    @Override
    public void delete() {
        super.delete();
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public Integer getId() { return id; }
    public Long getVersion() { return version; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public PromotionType getType() { return type; }
    public PromotionScope getScope() { return scope; }
    public double getDiscountValue() { return discountValue; }
    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public Double getMinOrderValue() { return minOrderValue; }
    public Integer getTargetProductId() { return targetProductId; }
    public Integer getUsageLimit() { return usageLimit; }
    public Integer getUsedCount() { return usedCount; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public boolean isActive() { return isActive; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Promotion p && id != null && id.equals(p.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}