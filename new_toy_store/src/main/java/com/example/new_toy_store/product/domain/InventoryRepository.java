package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.product.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByVariantId(Integer variantId);

    @Query("SELECT COALESCE(SUM(i.stockQuantity), 0) FROM Inventory i")
    long sumStockQuantity();

    @Query("SELECT COALESCE(SUM(i.reservedQuantity), 0) FROM Inventory i")
    long sumReservedQuantity();

    @Query("SELECT COUNT(i) FROM Inventory i WHERE (i.stockQuantity - i.reservedQuantity) <= :threshold")
    long countLowStock(@Param("threshold") int threshold);
}
