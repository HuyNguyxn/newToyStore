package com.example.new_toy_store.promotion.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class PromotionCrossModuleException extends PromotionDomainException {

    private PromotionCrossModuleException(String errorCode, String message, Map<String, Object> contextData) {
        super(HttpStatus.BAD_REQUEST, errorCode, message, contextData);
    }

    public static PromotionCrossModuleException invalidProductReference(Integer productId, String reason) {
        return new PromotionCrossModuleException(
                "PROMOTION_INVALID_PRODUCT_REFERENCE",
                "Dữ liệu sản phẩm gửi sang luồng khuyến mãi không hợp lệ.",
                Map.of(
                        "productId", productId,
                        "module", "product",
                        "reason", reason
                )
        );
    }

    public static PromotionCrossModuleException invalidOrderReference(Integer orderId, String promoCode, String reason) {
        return new PromotionCrossModuleException(
                "PROMOTION_INVALID_ORDER_REFERENCE",
                "Dữ liệu đơn hàng gửi sang luồng khuyến mãi không hợp lệ.",
                Map.of(
                        "orderId", orderId,
                        "promoCode", promoCode,
                        "module", "order",
                        "reason", reason
                )
        );
    }
}
