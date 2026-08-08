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
            SELECT 'OUTBOUND_SALE', 'Outbound from successful orders', COALESCE(SUM(i.quantity), 0), COALESCE(SUM(i.quantity * i.price), 0)
              FROM orders o
              JOIN order_items i ON i.order_id = o.id
             WHERE o.status IN ('COMPLETED', 'PARTIALLY_REFUNDED', 'FULLY_REFUNDED')
               AND o.created_at >= :from
               AND o.created_at < :to
               AND o.deleted_at IS NULL
               AND i.deleted_at IS NULL
            """, nativeQuery = true)
    List<Object[]> aggregateOutboundMovement(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
