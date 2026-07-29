package com.example.new_toy_store.payment.domain;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Integer> {

    Page<PaymentRefund> findByPaymentId(Integer paymentId, Pageable pageable);

    Page<PaymentRefund> findByOrderId(Integer orderId, Pageable pageable);

    boolean existsByRefundCode(String refundCode);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM PaymentRefund r WHERE r.paymentId = :paymentId AND r.status IN :statuses")
    double sumAmountByPaymentIdAndStatuses(@Param("paymentId") Integer paymentId, @Param("statuses") Collection<RefundStatus> statuses);

    @Query("SELECT COUNT(r) FROM PaymentRefund r WHERE r.status = :status AND r.createdAt >= :from AND r.createdAt < :to")
    long countByStatusBetween(@Param("status") RefundStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM PaymentRefund r WHERE r.status = :status AND r.createdAt >= :from AND r.createdAt < :to")
    double sumAmountByStatusBetween(@Param("status") RefundStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT FUNCTION('date', r.createdAt), COALESCE(SUM(r.amount), 0) FROM PaymentRefund r WHERE r.status = :status AND r.createdAt >= :from AND r.createdAt < :to GROUP BY FUNCTION('date', r.createdAt)")
    java.util.List<Object[]> aggregateDailyRefundAmount(@Param("status") RefundStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM PaymentRefund r WHERE r.id = :id")
    Optional<PaymentRefund> findByIdForUpdate(@Param("id") Integer id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE PaymentRefund r
               SET r.deletedAt = CURRENT_TIMESTAMP,
                   r.updatedAt = CURRENT_TIMESTAMP,
                   r.version = r.version + 1
             WHERE r.id = :id
               AND r.version = :version
               AND r.status IN :deletableStatuses
            """)
    int softDeleteWithVersion(
            @Param("id") Integer id,
            @Param("version") Long version,
            @Param("deletableStatuses") Collection<RefundStatus> deletableStatuses
    );
}
