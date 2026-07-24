package com.example.new_toy_store.promotion.application.facade;

import com.example.new_toy_store.promotion.application.PromotionService;
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class PromotionFacade {

    private final PromotionService promotionService;

    public PromotionFacade(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    public double calculateProductDiscount(Integer productId, double originalPrice) {
        return promotionService.calculateProductDiscount(productId, originalPrice);
    }

    public double calculateOrderDiscount(String promoCode, double cartTotal) {
        return promotionService.calculateOrderDiscount(promoCode, cartTotal);
    }

    public double calculateShippingDiscount(String promoCode, double currentShippingFee, double cartTotal) {
        return promotionService.calculateShippingDiscount(promoCode, currentShippingFee, cartTotal);
    }

    public List<PromotionResponse> getActivePromotionsForProducts(Set<Integer> productIds) {
        return promotionService.getActivePromotionsForProducts(productIds);
    }
}
