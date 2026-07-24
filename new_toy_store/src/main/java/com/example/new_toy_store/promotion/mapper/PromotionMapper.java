package com.example.new_toy_store.promotion.mapper;

import com.example.new_toy_store.promotion.application.dto.request.PromotionRequest;
import com.example.new_toy_store.promotion.application.dto.response.PromotionActionResponse;
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;
import com.example.new_toy_store.promotion.domain.Promotion;

import java.util.List;

public class PromotionMapper {

    private PromotionMapper() {}

    public static Promotion toEntity(PromotionRequest request) {
        Promotion promotion = new Promotion(
                request.getCode(),
                request.getName(),
                request.getType(),
                request.getScope(),
                request.getDiscountValue(),
                request.getStartDate(),
                request.getEndDate()
        );

        promotion.setupConditions(
                request.getMinOrderValue(),
                request.getMaxDiscountAmount(),
                request.getTargetProductId(),
                request.getUsageLimit()
        );

        return promotion;
    }

    public static PromotionResponse toResponse(Promotion promotion) {
        return toDetailResponse(promotion);
    }

    public static PromotionResponse toDetailResponse(Promotion promotion) {
        return createPromotionResponse(promotion, mapAllowedActions(promotion));
    }

    private static PromotionResponse createPromotionResponse(
            Promotion promotion,
            List<PromotionActionResponse> allowedActions
    ) {
        return new PromotionResponse(
                promotion.getId(),
                promotion.getVersion(),
                promotion.getCode(),
                promotion.getName(),
                promotion.getType(),
                promotion.getScope(),
                promotion.getDiscountValue(),
                promotion.getMaxDiscountAmount(),
                promotion.getMinOrderValue(),
                promotion.getTargetProductId(),
                promotion.getUsageLimit(),
                promotion.getUsedCount(),
                promotion.getStartDate(),
                promotion.getEndDate(),
                promotion.isActive(),
                allowedActions
        );
    }

    private static List<PromotionActionResponse> mapAllowedActions(Promotion promotion) {
        if (promotion.isActive()) {
            return List.of(
                    new PromotionActionResponse("UPDATE", "Cập nhật khuyến mãi"),
                    new PromotionActionResponse("DEACTIVATE", "Tạm ngưng khuyến mãi"),
                    new PromotionActionResponse("DELETE", "Xóa khuyến mãi")
            );
        }

        return List.of(
                new PromotionActionResponse("UPDATE", "Cập nhật khuyến mãi"),
                new PromotionActionResponse("ACTIVATE", "Kích hoạt khuyến mãi"),
                new PromotionActionResponse("DELETE", "Xóa khuyến mãi")
        );
    }
}
