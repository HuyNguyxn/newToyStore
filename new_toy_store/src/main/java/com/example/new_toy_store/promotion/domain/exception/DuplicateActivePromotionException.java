package com.example.new_toy_store.promotion.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateActivePromotionException extends PromotionDomainException {

    public DuplicateActivePromotionException(String code) {
        super(
                HttpStatus.CONFLICT,
                "DUPLICATE_ACTIVE_PROMOTION_CODE",
                "Mã khuyến mãi " + code + " đã tồn tại và đang hoạt động.",
                Map.of(
                        "code", code,
                        "reason", "DUPLICATE_ACTIVE_DATA"
                )
        );
    }
}
