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

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentTransaction, Integer>, JpaSpecificationExecutor<PaymentTransaction> {

    Optional<PaymentTransaction> findByOrderIdAndStatus(Integer orderId, PaymentStatus status);

    boolean existsByOrderIdAndStatus(Integer orderId, PaymentStatus status);

    boolean existsByOrderIdAndStatusIn(Integer orderId, List<PaymentStatus> statuses);

    Optional<PaymentTransaction> findFirstByOrderIdAndStatusInOrderByCreatedAtDesc(Integer orderId, Collection<PaymentStatus> statuses);

    Optional<PaymentTransaction> findByUserIdAndIdempotencyKey(Integer userId, String idempotencyKey);

    Optional<PaymentTransaction> findByOrderIdAndMethod(Integer orderId, PaymentMethod method);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PaymentTransaction p WHERE p.id = :id")
    Optional<PaymentTransaction> findByIdForUpdate(@Param("id") Integer id);

    Page<PaymentTransaction> findByUserId(Integer userId, Pageable pageable);

    Page<PaymentTransaction> findByOrderId(Integer orderId, Pageable pageable);

    @Query("SELECT p FROM PaymentTransaction p WHERE p.orderId = :orderId ORDER BY p.createdAt DESC")
    java.util.List<PaymentTransaction> findAllByOrderId(@Param("orderId") Integer orderId);

    @Query("SELECT COUNT(p) FROM PaymentTransaction p WHERE p.status = :status")
    long countByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM PaymentTransaction p WHERE p.status = :status")
    double sumAmountByStatus(@Param("status") PaymentStatus status);

    @Query("""
            SELECT COUNT(p)
              FROM PaymentTransaction p JOIN User u ON p.userId = u.id
             WHERE p.status = :status
               AND p.createdAt >= :from
               AND p.createdAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
            """)
    long countByStatusBetween(@Param("status") PaymentStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
              FROM PaymentTransaction p JOIN User u ON p.userId = u.id
             WHERE p.status = :status
               AND p.createdAt >= :from
               AND p.createdAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
            """)
    double sumAmountByStatusBetween(@Param("status") PaymentStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT p.method, COUNT(p), COALESCE(SUM(p.amount), 0)
              FROM PaymentTransaction p JOIN User u ON p.userId = u.id
             WHERE p.status = :status
               AND p.createdAt >= :from
               AND p.createdAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
             GROUP BY p.method
            """)
    java.util.List<Object[]> aggregateAmountByMethod(@Param("status") PaymentStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(p.failureReason, 'UNKNOWN'), COALESCE(p.failureReason, 'Unknown'), COUNT(p), 0
              FROM PaymentTransaction p JOIN User u ON p.userId = u.id
             WHERE p.status = :status
               AND p.createdAt >= :from
               AND p.createdAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
             GROUP BY p.failureReason
             ORDER BY COUNT(p) DESC
            """)
    java.util.List<Object[]> aggregateFailureReasons(@Param("status") PaymentStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

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
