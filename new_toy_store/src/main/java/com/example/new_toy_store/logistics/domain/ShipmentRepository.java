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

import java.util.Collection;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Integer>, JpaSpecificationExecutor<Shipment> {

    @Override
    Page<Shipment> findAll(Specification<Shipment> spec, Pageable pageable);

    Page<Shipment> findByUserId(Integer userId, Pageable pageable);

    Optional<Shipment> findByOrderId(Integer orderId);

    boolean existsByOrderId(Integer orderId);

    boolean existsByTrackingCode(String trackingCode);

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
                   s.updatedAt = CURRENT_TIMESTAMP,
                   s.version = s.version + 1
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
