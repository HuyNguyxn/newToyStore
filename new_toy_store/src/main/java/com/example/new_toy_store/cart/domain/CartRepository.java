package com.example.new_toy_store.cart.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Integer>, JpaSpecificationExecutor<Cart> {

    @EntityGraph(attributePaths = {"items"})
    Optional<Cart> findByUserId(Integer userId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE Cart c SET c.version = c.version + 1, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :cartId")
    void incrementVersion(@Param("cartId") Integer cartId);
}