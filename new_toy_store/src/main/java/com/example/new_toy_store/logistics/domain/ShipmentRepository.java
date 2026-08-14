package com.example.new_toy_store.logistics.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Integer>, JpaSpecificationExecutor<Shipment> {

    Optional<Shipment> findByTrackingCode(String trackingCode);

    boolean existsByTrackingCode(String trackingCode);

    Optional<Shipment> findByOrderId(Integer orderId);

    boolean existsByOrderId(Integer orderId);

    boolean existsByCustomerReturnId(Integer customerReturnId);

    boolean existsBySupplierReturnId(Integer supplierReturnId);

    Page<Shipment> findByUserId(Integer userId, Pageable pageable);

    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.status = :status")
    long countByStatus(@Param("status") ShipmentStatus status);

    @Query("""
            SELECT COUNT(s)
              FROM Shipment s JOIN Order o ON s.orderId = o.id JOIN User u ON o.userId = u.id
             WHERE s.status = :status
               AND s.createdAt >= :from
               AND s.createdAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
            """)
    long countByStatusBetween(@Param("status") ShipmentStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT s.providerCode, s.providerCode, COUNT(s), COALESCE(SUM(s.shippingFee), 0)
              FROM Shipment s JOIN Order o ON s.orderId = o.id JOIN User u ON o.userId = u.id
             WHERE s.createdAt >= :from
               AND s.createdAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
             GROUP BY s.providerCode
             ORDER BY COUNT(s) DESC
            """)
    java.util.List<Object[]> aggregateByProvider(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(s.failureReason, 'UNKNOWN'), COALESCE(s.failureReason, 'Unknown'), COUNT(s), 0
              FROM Shipment s JOIN Order o ON s.orderId = o.id JOIN User u ON o.userId = u.id
             WHERE s.status = :status
               AND s.createdAt >= :from
               AND s.createdAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
             GROUP BY s.failureReason
             ORDER BY COUNT(s) DESC
            """)
    java.util.List<Object[]> aggregateFailureReasons(@Param("status") ShipmentStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query(value = """
            SELECT region, region, COUNT(*), COALESCE(SUM(shipping_fee), 0)
              FROM (
                    SELECT TRIM(SUBSTRING_INDEX(s.shipping_address_snapshot, ',', -1)) AS region,
                           s.shipping_fee
                      FROM shipments s
                      JOIN orders o ON o.id = s.order_id AND o.deleted_at IS NULL
                      JOIN users u ON u.id = o.user_id AND u.role = 'CUSTOMER' AND u.deleted_at IS NULL
                     WHERE s.created_at >= :from
                       AND s.created_at < :to
                       AND s.deleted_at IS NULL
                   ) region_shipments
             GROUP BY region
             ORDER BY COUNT(*) DESC
            """, nativeQuery = true)
    java.util.List<Object[]> aggregateByRegion(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "items")
    Optional<Shipment> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Shipment s WHERE s.id = :id")
    Optional<Shipment> findByIdForUpdate(@Param("id") Integer id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Shipment s
               SET s.deletedAt = CURRENT_TIMESTAMP,
                   s.updatedAt = CURRENT_TIMESTAMP
             WHERE s.id = :id
               AND s.version = :version
               AND s.status IN :deletableStatuses
            """)
    int softDeleteWithVersion(
            @Param("id") Integer id,
            @Param("version") Long version,
            @Param("deletableStatuses") Collection<ShipmentStatus> deletableStatuses
    );
}
