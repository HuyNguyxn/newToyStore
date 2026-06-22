package com.example.new_toy_store.promotion.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "promotions",
        indexes = {
                @Index(name = "idx_promo_code", columnList = "code", unique = true),
                @Index(name = "idx_promo_time_status", columnList = "is_active, start_date, end_date")
        }
)
public class Promotion extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
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

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    protected Promotion() {}

    public Promotion(String code, String name, PromotionType type, PromotionScope scope, double discountValue, LocalDateTime startDate, LocalDateTime endDate) {
        this.code = code.toUpperCase().trim();
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.discountValue = discountValue;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @PrePersist
    @PreUpdate
    private void validateEntityState() {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalStateException("Ngày bắt đầu không được vượt quá ngày kết thúc");
        }
        if (discountValue < 0) {
            throw new IllegalStateException("Giá trị giảm giá không được âm");
        }
        if (type == PromotionType.PERCENTAGE && discountValue > 100) {
            throw new IllegalStateException("Giảm giá phần trăm không được vượt quá 100%");
        }
    }

    public void setupConditions(Double minOrderValue, Double maxDiscountAmount, Integer targetProductId) {
        this.scope.validateSetup(minOrderValue, targetProductId);
        this.minOrderValue = minOrderValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.targetProductId = targetProductId;
    }

    public boolean isCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        return this.isActive && startDate != null && endDate != null && !now.isBefore(startDate) && !now.isAfter(endDate);
    }

    public boolean isApplicableForOrder(double currentOrderTotal) {
        if (!isCurrentlyValid() || this.scope != PromotionScope.ORDER) {
            return false;
        }
        return this.minOrderValue == null || currentOrderTotal >= this.minOrderValue;
    }

    public double applyDiscount(double originalAmount) {
        if (!isCurrentlyValid()) {
            return 0.0;
        }
        double discount = this.type.calculateDiscount(originalAmount, this.discountValue, this.maxDiscountAmount);
        return discount > originalAmount ? originalAmount : discount;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public Integer getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public PromotionType getType() { return type; }
    public PromotionScope getScope() { return scope; }
    public double getDiscountValue() { return discountValue; }
    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public Double getMinOrderValue() { return minOrderValue; }
    public Integer getTargetProductId() { return targetProductId; }
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