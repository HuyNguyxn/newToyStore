package com.example.new_toy_store.promotion.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class PromotionDeletedConflictException extends PromotionDomainException {

    public PromotionDeletedConflictException(Integer promotionId, String action) {
        super(
                HttpStatus.CONFLICT,
                "PROMOTION_DELETED_CONFLICT",
                "Khuyến mãi ID " + promotionId + " đã bị xóa mềm nên không thể " + action + ".",
                Map.of(
                        "promotionId", promotionId,
                        "action", action,
                        "reason", "SOFT_DELETED"
                )
        );
    }
}
