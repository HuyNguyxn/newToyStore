package com.example.new_toy_store.promotion.application.dto.response;

import com.example.new_toy_store.promotion.domain.PromotionScope;
import com.example.new_toy_store.promotion.domain.PromotionType;

import java.time.LocalDateTime;
import java.util.List;

public class PromotionResponse {
    private Integer id;
    private Long version;
    private String code;
    private String name;
    private PromotionType type;
    private PromotionScope scope;
    private double discountValue;
    private Double maxDiscountAmount;
    private Double minOrderValue;
    private Integer targetProductId;
    private Integer usageLimit;
    private Integer usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;
    private List<PromotionActionResponse> allowedNextActions;

    public PromotionResponse(Integer id,
                             Long version,
                             String code,
                             String name,
                             PromotionType type,
                             PromotionScope scope,
                             double discountValue,
                             Double maxDiscountAmount,
                             Double minOrderValue,
                             Integer targetProductId,
                             Integer usageLimit,
                             Integer usedCount,
                             LocalDateTime startDate,
                             LocalDateTime endDate,
                             boolean isActive,
                             List<PromotionActionResponse> allowedNextActions) {
        this.id = id;
        this.version = version;
        this.code = code;
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.discountValue = discountValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minOrderValue = minOrderValue;
        this.targetProductId = targetProductId;
        this.usageLimit = usageLimit;
        this.usedCount = usedCount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
        this.allowedNextActions = allowedNextActions;
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
    public List<PromotionActionResponse> getAllowedNextActions() { return allowedNextActions; }
}
