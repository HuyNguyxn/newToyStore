package com.example.new_toy_store.promotion.application;

import com.example.new_toy_store.promotion.application.dto.request.PromotionRequest;
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;
import com.example.new_toy_store.promotion.domain.Promotion;
import com.example.new_toy_store.promotion.domain.PromotionRepository;
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chương trình khuyến mãi"));
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
                .orElseThrow(() -> new RuntimeException("Mã khuyến mãi không hợp lệ"));

        if (!promotion.isApplicableForOrder(cartTotal)) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt điều kiện áp dụng mã khuyến mãi này");
        }

        return promotion.applyDiscount(cartTotal);
    }
}