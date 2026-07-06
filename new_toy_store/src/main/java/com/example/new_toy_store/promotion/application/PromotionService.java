package com.example.new_toy_store.promotion.application;

import com.example.new_toy_store.promotion.application.dto.request.PromotionRequest;
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;
import com.example.new_toy_store.promotion.domain.Promotion;
import com.example.new_toy_store.promotion.domain.PromotionRepository;
import com.example.new_toy_store.promotion.domain.PromotionScope;
import com.example.new_toy_store.promotion.mapper.PromotionMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public PromotionResponse updatePromotion(Integer id, PromotionRequest request) {
        Promotion promotion = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương trình khuyến mãi"));

        promotion.updateDetails(
                request.getName(),
                request.getDiscountValue(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMinOrderValue(),
                request.getMaxDiscountAmount(),
                request.getTargetProductId()
        );

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
    public PromotionResponse getPromotionById(Integer id) {
        return repository.findById(id)
                .map(PromotionMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chương trình khuyến mãi"));
    }

    @Transactional(readOnly = true)
    public Page<PromotionResponse> getPromotions(String scopeStr, Boolean isActive, Pageable pageable) {
        PromotionScope scope = null;
        if (scopeStr != null && !scopeStr.trim().isEmpty()) {
            scope = PromotionScope.from(scopeStr);
        }
        return repository.findAllWithFilters(scope, isActive, pageable)
                .map(PromotionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public double calculateProductDiscount(Integer productId, double originalPrice) {
        if (originalPrice <= 0 || productId == null) {
            return 0.0;
        }
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
        if (promoCode == null || promoCode.trim().isEmpty() || cartTotal <= 0) {
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
        if (promoCode == null || promoCode.trim().isEmpty() || currentShippingFee <= 0) {
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

    @Transactional(readOnly = true)
    public List<Promotion> getActivePromotionsForProducts(Set<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) return List.of();
        return repository.findByScopeAndTargetProductIdIn(PromotionScope.PRODUCT, productIds)
                .stream()
                .filter(Promotion::isCurrentlyValid)
                .collect(Collectors.toList());
    }
}