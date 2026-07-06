package com.example.new_toy_store.promotion.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    Optional<Promotion> findByCode(String code);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND (p.startDate IS NULL OR p.startDate <= :now) AND (p.endDate IS NULL OR p.endDate >= :now) AND p.scope = 'PRODUCT' AND p.targetProductId = :productId")
    List<Promotion> findActivePromotionsForProduct(@Param("productId") Integer productId, @Param("now") LocalDateTime now);

    List<Promotion> findByScopeAndTargetProductIdIn(PromotionScope scope, Set<Integer> targetProductIds);

    @Query("SELECT p FROM Promotion p WHERE (:scope IS NULL OR p.scope = :scope) AND (:isActive IS NULL OR p.isActive = :isActive)")
    Page<Promotion> findAllWithFilters(@Param("scope") PromotionScope scope, @Param("isActive") Boolean isActive, Pageable pageable);
}