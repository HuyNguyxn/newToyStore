package com.example.new_toy_store.cart.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Integer>, JpaSpecificationExecutor<CartItem> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CartItem c WHERE c.updatedAt < :thresholdDate")
    int deleteExpiredItems(@Param("thresholdDate") LocalDateTime thresholdDate);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE CartItem c SET c.addedPrice = :newPrice WHERE c.variantId = :variantId")
    int updatePriceByVariantId(@Param("variantId") Integer variantId, @Param("newPrice") double newPrice);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CartItem c WHERE c.variantId = :variantId")
    int deleteByVariantId(@Param("variantId") Integer variantId);

    @Override
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = "cart")
    List<CartItem> findAll(Specification<CartItem> specification);
}
