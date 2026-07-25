package com.example.new_toy_store.payment.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentTransaction, Integer>, JpaSpecificationExecutor<PaymentTransaction> {

    @Override
    Optional<PaymentTransaction> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentTransaction p WHERE p.id = :id")
    Optional<PaymentTransaction> findByIdForUpdate(@Param("id") Integer id);

    Page<PaymentTransaction> findByUserId(Integer userId, Pageable pageable);

    Optional<PaymentTransaction> findByOrderId(Integer orderId);

    Optional<PaymentTransaction> findByOrderIdAndMethod(Integer orderId, PaymentMethod method);

    boolean existsByOrderIdAndStatusIn(Integer orderId, Collection<PaymentStatus> statuses);

    @Override
    Page<PaymentTransaction> findAll(Specification<PaymentTransaction> spec, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE PaymentTransaction p
               SET p.deletedAt = CURRENT_TIMESTAMP,
                   p.updatedAt = CURRENT_TIMESTAMP,
                   p.version = p.version + 1
             WHERE p.id = :id
               AND p.version = :version
               AND p.status IN :deletableStatuses
            """)
    int softDeleteWithVersion(
            @Param("id") Integer id,
            @Param("version") Long version,
            @Param("deletableStatuses") Collection<PaymentStatus> deletableStatuses
    );
}
