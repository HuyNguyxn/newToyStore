package com.example.new_toy_store.promotion.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PromotionRepository extends JpaRepository<Promotion, Integer>, JpaSpecificationExecutor<Promotion> {

    Optional<Promotion> findByCode(String code);

    long countByIsActive(boolean active);

    @Query("SELECT COALESCE(SUM(p.usedCount), 0) FROM Promotion p")
    long sumUsedCount();

    Page<Promotion> findAll(Specification<Promotion> specification, Pageable pageable);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND (p.startDate IS NULL OR p.startDate <= :now) AND (p.endDate IS NULL OR p.endDate >= :now) AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit) AND p.scope = 'PRODUCT' AND p.targetProductId = :productId")
    List<Promotion> findActivePromotionsForProduct(@Param("productId") Integer productId, @Param("now") LocalDateTime now);

    @Query("SELECT p FROM Promotion p WHERE p.isActive = true AND (p.startDate IS NULL OR p.startDate <= :now) AND (p.endDate IS NULL OR p.endDate >= :now) AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit) AND p.scope = 'ORDER' ORDER BY p.endDate ASC, p.id ASC")
    List<Promotion> findAvailableOrderPromotions(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Promotion p WHERE p.scope = :scope AND p.targetProductId IN :targetProductIds AND p.isActive = true AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)")
    List<Promotion> findByScopeAndTargetProductIdIn(@Param("scope") PromotionScope scope, @Param("targetProductIds") Set<Integer> targetProductIds);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Promotion p
               SET p.isActive = :active,
                   p.updatedAt = CURRENT_TIMESTAMP,
                   p.version = p.version + 1
             WHERE p.id = :id
               AND p.version = :version
            """)
    int updateActiveWithVersion(@Param("id") Integer id,
                                @Param("version") Long version,
                                @Param("active") boolean active);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Promotion p
               SET p.isActive = false,
                   p.deletedAt = CURRENT_TIMESTAMP,
                   p.updatedAt = CURRENT_TIMESTAMP,
                   p.version = p.version + 1
             WHERE p.id = :id
               AND p.version = :version
            """)
    int softDeleteWithVersion(@Param("id") Integer id, @Param("version") Long version);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Promotion p
               SET p.usedCount = p.usedCount + 1,
                   p.updatedAt = CURRENT_TIMESTAMP,
                   p.version = p.version + 1
             WHERE p.id = :id
               AND p.version = :version
               AND p.isActive = true
               AND (p.startDate IS NULL OR p.startDate <= :now)
               AND (p.endDate IS NULL OR p.endDate >= :now)
               AND (p.usageLimit IS NULL OR p.usedCount < p.usageLimit)
            """)
    int incrementUsedCountWithVersion(@Param("id") Integer id,
                                      @Param("version") Long version,
                                      @Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Promotion p
               SET p.usedCount = p.usedCount - 1,
                   p.updatedAt = CURRENT_TIMESTAMP,
                   p.version = p.version + 1
             WHERE p.id = :id
               AND p.version = :version
               AND p.usedCount > 0
            """)
    int decrementUsedCountWithVersion(@Param("id") Integer id, @Param("version") Long version);
}
