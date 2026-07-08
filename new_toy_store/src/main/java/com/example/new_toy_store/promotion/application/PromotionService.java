package com.example.new_toy_store.promotion.application;

import com.example.new_toy_store.promotion.application.dto.request.PromotionRequest;
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;
import com.example.new_toy_store.promotion.domain.Promotion;
import com.example.new_toy_store.promotion.domain.PromotionRepository;
import com.example.new_toy_store.promotion.domain.PromotionScope;
import com.example.new_toy_store.promotion.domain.exception.InvalidPromotionOperationException;
import com.example.new_toy_store.promotion.domain.exception.PromotionNotFoundException;
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
        String cleanCode = request.getCode().toUpperCase().trim();
        if (repository.findByCode(cleanCode).isPresent()) {
            throw InvalidPromotionOperationException.codeExists(cleanCode);
        }

        Promotion promotion = PromotionMapper.toEntity(request);
        repository.save(promotion);
        return PromotionMapper.toResponse(promotion);
    }

    @Transactional
    public PromotionResponse updatePromotion(Integer id, PromotionRequest request) {
        Promotion promotion = repository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));

        promotion.updateDetails(
                request.getName(),
                request.getDiscountValue(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMinOrderValue(),
                request.getMaxDiscountAmount(),
                request.getTargetProductId(),
                request.getUsageLimit()
        );

        repository.save(promotion);
        return PromotionMapper.toResponse(promotion);
    }

    @Transactional
    public void activatePromotion(Integer id) {
        Promotion promotion = repository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));
        promotion.activate();
        repository.save(promotion);
    }

    @Transactional
    public void deactivatePromotion(Integer id) {
        Promotion promotion = repository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));
        promotion.deactivate();
        repository.save(promotion);
    }

    @Transactional
    public void deletePromotion(Integer id) {
        Promotion promotion = repository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));
        promotion.delete();
        repository.save(promotion);
    }

    @Transactional
    public void consumePromotion(String promoCode) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            throw InvalidPromotionOperationException.nullPromoCode();
        }
        String cleanCode = promoCode.toUpperCase().trim();
        Promotion promotion = repository.findByCode(cleanCode)
                .orElseThrow(() -> new PromotionNotFoundException(cleanCode));
        promotion.incrementUsedCount();
        repository.save(promotion);
    }

    @Transactional
    public void releasePromotion(String promoCode) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            throw InvalidPromotionOperationException.nullPromoCode();
        }
        String cleanCode = promoCode.toUpperCase().trim();
        Promotion promotion = repository.findByCode(cleanCode)
                .orElseThrow(() -> new PromotionNotFoundException(cleanCode));
        promotion.decrementUsedCount();
        repository.save(promotion);
    }

    @Transactional(readOnly = true)
    public PromotionResponse getPromotionById(Integer id) {
        return repository.findById(id)
                .map(PromotionMapper::toResponse)
                .orElseThrow(() -> new PromotionNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public PromotionResponse getPromotionByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw InvalidPromotionOperationException.nullPromoCode();
        }
        String cleanCode = code.toUpperCase().trim();
        return repository.findByCode(cleanCode)
                .map(PromotionMapper::toResponse)
                .orElseThrow(() -> new PromotionNotFoundException(cleanCode));
    }

    @Transactional(readOnly = true)
    public Page<PromotionResponse> getPromotions(String scopeStr, Boolean isActive, String keyword, Pageable pageable) {
        PromotionScope scope = null;
        if (scopeStr != null && !scopeStr.trim().isEmpty()) {
            scope = PromotionScope.from(scopeStr);
        }
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return repository.findAllWithFilters(scope, isActive, cleanKeyword, pageable)
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

        String cleanCode = promoCode.toUpperCase().trim();
        Promotion promotion = repository.findByCode(cleanCode)
                .orElseThrow(() -> new PromotionNotFoundException(cleanCode));

        if (promotion.getScope() != PromotionScope.ORDER) {
            throw InvalidPromotionOperationException.scopeMismatch();
        }

        if (!promotion.isApplicableForOrder(cartTotal)) {
            throw InvalidPromotionOperationException.minOrderNotMet();
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
    public List<PromotionResponse> getActivePromotionsForProducts(Set<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return repository.findByScopeAndTargetProductIdIn(PromotionScope.PRODUCT, productIds)
                .stream()
                .filter(Promotion::isCurrentlyValid)
                .map(PromotionMapper::toResponse)
                .collect(Collectors.toList());
    }
}