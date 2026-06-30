package com.example.new_toy_store.cart.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.updatedAt < :thresholdDate")
    void deleteExpiredItems(@Param("thresholdDate") LocalDateTime thresholdDate);

    List<CartItem> findByUpdatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}