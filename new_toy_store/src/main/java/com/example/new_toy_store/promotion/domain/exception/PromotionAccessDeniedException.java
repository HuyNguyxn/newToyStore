package com.example.new_toy_store.promotion.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class PromotionAccessDeniedException extends PromotionDomainException {

    public PromotionAccessDeniedException(Integer promotionId, Integer currentUserId, String action) {
        super(
                HttpStatus.FORBIDDEN,
                "PROMOTION_ACCESS_DENIED",
                "Người dùng ID " + currentUserId + " không có quyền " + action + " khuyến mãi ID " + promotionId + ".",
                Map.of(
                        "promotionId", promotionId,
                        "currentUserId", currentUserId,
                        "action", action
                )
        );
    }
}
