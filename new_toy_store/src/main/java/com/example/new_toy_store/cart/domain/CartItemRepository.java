package com.example.new_toy_store.cart.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Integer>, JpaSpecificationExecutor<CartItem> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CartItem c WHERE c.updatedAt < :thresholdDate")
    void deleteExpiredItems(@Param("thresholdDate") LocalDateTime thresholdDate);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE CartItem c SET c.addedPrice = :newPrice WHERE c.variantId = :variantId")
    void updatePriceByVariantId(@Param("variantId") Integer variantId, @Param("newPrice") double newPrice);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM CartItem c WHERE c.variantId = :variantId")
    void deleteByVariantId(@Param("variantId") Integer variantId);

    List<CartItem> findByUpdatedAtBetween(LocalDateTime start, LocalDateTime end);
}