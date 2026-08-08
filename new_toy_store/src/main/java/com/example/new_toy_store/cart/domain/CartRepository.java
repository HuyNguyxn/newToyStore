package com.example.new_toy_store.cart.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer>, JpaSpecificationExecutor<Cart> {

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = "items")
    Optional<Cart> findByUserId(Integer userId);

    @Override
    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = "items")
    Optional<Cart> findById(Integer id);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = "items")
    @Query("SELECT c FROM Cart c WHERE c.userId = :userId")
    Optional<Cart> findForUpdateByUserId(@Param("userId") Integer userId);

    @EntityGraph(type = EntityGraph.EntityGraphType.LOAD, attributePaths = "items")
    @Query("SELECT c FROM Cart c WHERE c.id = :cartId")
    Optional<Cart> findForUpdateById(@Param("cartId") Integer cartId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Cart c
               SET c.version = c.version + 1,
                   c.updatedAt = CURRENT_TIMESTAMP
             WHERE EXISTS (
                   SELECT item.id
                     FROM CartItem item
                    WHERE item.cart = c
                      AND item.variantId = :variantId
             )
            """)
    int touchCartsContainingVariant(@Param("variantId") Integer variantId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Cart c
               SET c.version = c.version + 1,
                   c.updatedAt = CURRENT_TIMESTAMP
             WHERE EXISTS (
                   SELECT item.id
                     FROM CartItem item
                    WHERE item.cart = c
                      AND item.updatedAt < :thresholdDate
             )
            """)
    int touchCartsContainingExpiredItems(@Param("thresholdDate") LocalDateTime thresholdDate);
}
