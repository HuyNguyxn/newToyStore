package com.example.new_toy_store.imports.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

public interface ImportNoteRepository extends JpaRepository<ImportNote, Integer>, JpaSpecificationExecutor<ImportNote> {

    long countByStatus(ImportStatus status);

    @EntityGraph(attributePaths = "items")
    @Query("SELECT i FROM ImportNote i WHERE i.id = :id")
    Optional<ImportNote> findByIdWithItems(@Param("id") Integer id);

    Page<ImportNote> findAll(Specification<ImportNote> specification, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ImportNote i
               SET i.status = :nextStatus,
                   i.updatedAt = CURRENT_TIMESTAMP,
                   i.version = i.version + 1
             WHERE i.id = :id
               AND i.version = :version
               AND i.status = :currentStatus
            """)
    int updateStatusWithVersion(@Param("id") Integer id,
                                @Param("version") Long version,
                                @Param("currentStatus") ImportStatus currentStatus,
                                @Param("nextStatus") ImportStatus nextStatus);

    @Query(value = """
            SELECT 'INBOUND_IMPORT', 'Inbound from completed imports', COALESCE(SUM(ii.quantity), 0), COALESCE(SUM(ii.quantity * ii.import_price), 0)
              FROM import_notes n
              JOIN import_note_items ii ON ii.import_note_id = n.id
             WHERE n.status = 'COMPLETED'
               AND n.updated_at >= :from
               AND n.updated_at < :to
               AND n.deleted_at IS NULL
            """, nativeQuery = true)
    List<Object[]> aggregateInboundMovement(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT DATE(n.updated_at),
                   COALESCE(SUM(ii.quantity), 0),
                   COALESCE(SUM(ii.quantity * ii.import_price), 0)
              FROM import_notes n
              JOIN import_note_items ii ON ii.import_note_id = n.id
             WHERE n.status = 'COMPLETED'
               AND n.updated_at >= :from
               AND n.updated_at < :to
               AND n.deleted_at IS NULL
             GROUP BY DATE(n.updated_at)
            """, nativeQuery = true)
    List<Object[]> aggregateDailyInboundMovement(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT COALESCE(
                       SUM(ii.quantity * ii.import_price) / NULLIF(SUM(ii.quantity), 0),
                       0
                   )
              FROM import_notes n
              JOIN import_note_items ii ON ii.import_note_id = n.id
             WHERE n.status = 'COMPLETED'
               AND n.deleted_at IS NULL
               AND (:variantId IS NULL OR ii.variant_id = :variantId)
             GROUP BY n.id, n.updated_at
             ORDER BY n.updated_at DESC, n.id DESC
             LIMIT 1
            """, nativeQuery = true)
    List<Double> findLatestCompletedImportAveragePrice(@Param("variantId") Integer variantId);

    @Query(value = """
            SELECT 'OUTBOUND_SALE', 'Outbound from completed orders', COALESCE(SUM(i.quantity), 0),
                   COALESCE(SUM(i.quantity * COALESCE(NULLIF(i.cost_price_snapshot, 0), pv.cost_price, 0)), 0)
              FROM orders o
              JOIN order_items i ON i.order_id = o.id
              LEFT JOIN product_variants pv ON pv.id = i.variant_id
              JOIN (
                    SELECT order_id, MIN(created_at) AS completed_at
                      FROM order_histories
                     WHERE status = 'COMPLETED'
                       AND deleted_at IS NULL
                     GROUP BY order_id
              ) completed ON completed.order_id = o.id
              JOIN users u ON u.id = o.user_id
                          AND (u.role IS NULL OR u.role = 'CUSTOMER')
                          AND u.deleted_at IS NULL
             WHERE o.status IN ('COMPLETED', 'PARTIALLY_REFUNDED', 'FULLY_REFUNDED')
               AND completed.completed_at >= :from
               AND completed.completed_at < :to
               AND o.deleted_at IS NULL
               AND i.deleted_at IS NULL
            """, nativeQuery = true)
    List<Object[]> aggregateOutboundMovement(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
