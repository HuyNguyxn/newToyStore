package com.example.new_toy_store.promotion.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

    Optional<Promotion> findByCode(String code);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND p.startDate <= :now AND p.endDate >= :now AND p.scope = 'PRODUCT' AND p.targetProductId = :productId")
    List<Promotion> findActivePromotionsForProduct(@Param("productId") Integer productId, @Param("now") LocalDateTime now);

    List<Promotion> findByScopeAndTargetProductIdIn(PromotionScope scope, Set<Integer> targetProductIds);
}