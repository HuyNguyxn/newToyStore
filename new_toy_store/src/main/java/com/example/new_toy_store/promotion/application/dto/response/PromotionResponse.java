package com.example.new_toy_store.promotion.application.dto.response;

import java.time.LocalDateTime;

public class PromotionResponse {
    private Integer id;
    private Long version;
    private String code;
    private String name;
    private String type;
    private String typeDescription;
    private String scope;
    private String scopeDescription;
    private double discountValue;
    private Double maxDiscountAmount;
    private Double minOrderValue;
    private Integer targetProductId;
    private Integer usageLimit;
    private Integer usedCount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;

    public PromotionResponse(Integer id, Long version, String code, String name, String type, String typeDescription, String scope, String scopeDescription, double discountValue, Double maxDiscountAmount, Double minOrderValue, Integer targetProductId, Integer usageLimit, Integer usedCount, LocalDateTime startDate, LocalDateTime endDate, boolean isActive) {
        this.id = id;
        this.version = version;
        this.code = code;
        this.name = name;
        this.type = type;
        this.typeDescription = typeDescription;
        this.scope = scope;
        this.scopeDescription = scopeDescription;
        this.discountValue = discountValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minOrderValue = minOrderValue;
        this.targetProductId = targetProductId;
        this.usageLimit = usageLimit;
        this.usedCount = usedCount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isActive = isActive;
    }

    public Integer getId() { return id; }
    public Long getVersion() { return version; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getTypeDescription() { return typeDescription; }
    public String getScope() { return scope; }
    public String getScopeDescription() { return scopeDescription; }
    public double getDiscountValue() { return discountValue; }
    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public Double getMinOrderValue() { return minOrderValue; }
    public Integer getTargetProductId() { return targetProductId; }
    public Integer getUsageLimit() { return usageLimit; }
    public Integer getUsedCount() { return usedCount; }
    public LocalDateTime getStartDate() { return startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public boolean isActive() { return isActive; }
}