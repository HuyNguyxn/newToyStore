package com.example.new_toy_store.promotion.application;

import com.example.new_toy_store.promotion.application.dto.request.PromotionRequest;
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;
import com.example.new_toy_store.promotion.domain.Promotion;
import com.example.new_toy_store.promotion.domain.PromotionRepository;
import com.example.new_toy_store.promotion.domain.PromotionScope;
import com.example.new_toy_store.promotion.mapper.PromotionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PromotionService {

    private final PromotionRepository repository;

    public PromotionService(PromotionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PromotionResponse createPromotion(PromotionRequest request) {
        if (repository.findByCode(request.getCode().toUpperCase().trim()).isPresent()) {
            throw new IllegalArgumentException("Mã khuyến mãi đã tồn tại");
        }

        Promotion promotion = PromotionMapper.toEntity(request);
        repository.save(promotion);
        return PromotionMapper.toResponse(promotion);
    }

    @Transactional
    public void deactivatePromotion(Integer id) {
        Promotion promotion = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương trình khuyến mãi"));
        promotion.deactivate();
        repository.save(promotion);
    }

    @Transactional(readOnly = true)
    public double calculateProductDiscount(Integer productId, double originalPrice) {
        List<Promotion> activePromos = repository.findActivePromotionsForProduct(productId, LocalDateTime.now());

        if (activePromos.isEmpty()) {
            return 0.0;
        }

        return activePromos.stream()
                .filter(promo -> promo.getScope() == PromotionScope.PRODUCT)
                .filter(Promotion::isCurrentlyValid)
                .map(promo -> promo.applyDiscount(originalPrice))
                .max(Double::compareTo)
                .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public double calculateOrderDiscount(String promoCode, double cartTotal) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return 0.0;
        }

        Promotion promotion = repository.findByCode(promoCode.toUpperCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Mã khuyến mãi không hợp lệ hoặc đã hết hạn"));

        if (promotion.getScope() != PromotionScope.ORDER) {
            return 0.0;
        }

        if (!promotion.isApplicableForOrder(cartTotal)) {
            throw new IllegalStateException("Đơn hàng chưa đạt điều kiện tối thiểu để áp dụng mã này");
        }

        return promotion.applyDiscount(cartTotal);
    }

    @Transactional(readOnly = true)
    public double calculateShippingDiscount(String promoCode, double currentShippingFee, double cartTotal) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return 0.0;
        }

        Promotion promotion = repository.findByCode(promoCode.toUpperCase().trim())
                .orElse(null);

        if (promotion == null || !promotion.isCurrentlyValid() || promotion.getScope() != PromotionScope.SHIPPING) {
            return 0.0;
        }

        if (promotion.getMinOrderValue() != null && cartTotal < promotion.getMinOrderValue()) {
            return 0.0;
        }

        return promotion.applyDiscount(currentShippingFee);
    }
}