package com.example.new_toy_store.promotion.mapper;

import com.example.new_toy_store.promotion.application.dto.request.PromotionRequest;
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;
import com.example.new_toy_store.promotion.domain.Promotion;
import com.example.new_toy_store.promotion.domain.PromotionScope;
import com.example.new_toy_store.promotion.domain.PromotionType;

public class PromotionMapper {

    public static Promotion toEntity(PromotionRequest request) {
        Promotion promotion = new Promotion(
                request.getCode(),
                request.getName(),
                PromotionType.from(request.getType()),
                PromotionScope.from(request.getScope()),
                request.getDiscountValue(),
                request.getStartDate(),
                request.getEndDate()
        );

        promotion.setupConditions(request.getMinOrderValue(), request.getMaxDiscountAmount(), request.getTargetProductId());

        return promotion;
    }

    public static PromotionResponse toResponse(Promotion promotion) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getCode(),
                promotion.getName(),
                promotion.getType().name(),
                promotion.getType().getDescription(),
                promotion.getScope().name(),
                promotion.getScope().getDescription(),
                promotion.getDiscountValue(),
                promotion.getMaxDiscountAmount(),
                promotion.getMinOrderValue(),
                promotion.getTargetProductId(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.isActive()
        );
    }
}