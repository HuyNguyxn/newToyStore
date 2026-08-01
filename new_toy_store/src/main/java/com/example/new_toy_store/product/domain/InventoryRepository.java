package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.product.domain.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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

    @Query(value = """
            SELECT 'CURRENT_STOCK', 'Current stock on hand', COALESCE(SUM(i.stock_quantity), 0), COALESCE(SUM(i.stock_quantity * COALESCE(v.cost_price, 0)), 0)
              FROM inventories i
              LEFT JOIN product_variants v ON v.id = i.variant_id
             WHERE i.deleted_at IS NULL
            UNION ALL
            SELECT 'RESERVED_STOCK', 'Reserved stock', COALESCE(SUM(i.reserved_quantity), 0), COALESCE(SUM(i.reserved_quantity * COALESCE(v.cost_price, 0)), 0)
              FROM inventories i
              LEFT JOIN product_variants v ON v.id = i.variant_id
             WHERE i.deleted_at IS NULL
            UNION ALL
            SELECT 'AVAILABLE_STOCK', 'Available stock', COALESCE(SUM(i.stock_quantity - i.reserved_quantity), 0), COALESCE(SUM((i.stock_quantity - i.reserved_quantity) * COALESCE(v.cost_price, 0)), 0)
              FROM inventories i
              LEFT JOIN product_variants v ON v.id = i.variant_id
             WHERE i.deleted_at IS NULL
            UNION ALL
            SELECT 'LOW_STOCK_VARIANTS', 'Low stock variants', COUNT(*), 0
              FROM inventories i
             WHERE i.deleted_at IS NULL
               AND (i.stock_quantity - i.reserved_quantity) <= :threshold
            """, nativeQuery = true)
    List<Object[]> aggregateCurrentStockSnapshots(@Param("threshold") int threshold);
}
