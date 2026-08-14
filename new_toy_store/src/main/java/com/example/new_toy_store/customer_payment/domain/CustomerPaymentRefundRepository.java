package com.example.new_toy_store.customer_payment.domain;

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

public interface CustomerPaymentRefundRepository extends JpaRepository<CustomerPaymentRefund, Integer> {

    Page<CustomerPaymentRefund> findByPaymentId(Integer paymentId, Pageable pageable);

    Page<CustomerPaymentRefund> findByOrderId(Integer orderId, Pageable pageable);

    boolean existsByRefundCode(String refundCode);

    Optional<CustomerPaymentRefund> findByRefundCode(String refundCode);

    Optional<CustomerPaymentRefund> findFirstByRefundCodeStartingWithOrderByCreatedAtDesc(String refundCodePrefix);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM CustomerPaymentRefund r WHERE r.paymentId = :paymentId AND r.status IN :statuses")
    double sumAmountByPaymentIdAndStatuses(@Param("paymentId") Integer paymentId, @Param("statuses") Collection<RefundStatus> statuses);

    @Query("""
            SELECT COUNT(r)
              FROM CustomerPaymentRefund r JOIN Order o ON r.orderId = o.id JOIN User u ON o.userId = u.id
             WHERE r.status = :status
               AND r.createdAt >= :from
               AND r.createdAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
            """)
    long countByStatusBetween(@Param("status") RefundStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(r.amount), 0)
              FROM CustomerPaymentRefund r JOIN Order o ON r.orderId = o.id JOIN User u ON o.userId = u.id
             WHERE r.status = :status
               AND r.completedAt >= :from
               AND r.completedAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
            """)
    double sumAmountByStatusBetween(@Param("status") RefundStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(DISTINCT r.orderId)
              FROM CustomerPaymentRefund r JOIN Order o ON r.orderId = o.id JOIN User u ON o.userId = u.id
             WHERE r.status = :status
               AND r.completedAt >= :from
               AND r.completedAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
            """)
    long countDistinctOrdersByStatusCompletedBetween(
            @Param("status") RefundStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
            SELECT FUNCTION('date', r.completedAt), COALESCE(SUM(r.amount), 0)
              FROM CustomerPaymentRefund r JOIN Order o ON r.orderId = o.id JOIN User u ON o.userId = u.id
             WHERE r.status = :status
               AND r.completedAt >= :from
               AND r.completedAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
             GROUP BY FUNCTION('date', r.completedAt)
            """)
    java.util.List<Object[]> aggregateDailyRefundAmount(@Param("status") RefundStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(r.reason, 'UNKNOWN'), COALESCE(r.reason, 'Unknown'), COUNT(r), COALESCE(SUM(r.amount), 0)
              FROM CustomerPaymentRefund r JOIN Order o ON r.orderId = o.id JOIN User u ON o.userId = u.id
             WHERE r.status = com.example.new_toy_store.customer_payment.domain.RefundStatus.SUCCEEDED
               AND r.completedAt >= :from
               AND r.completedAt < :to
               AND u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER
             GROUP BY r.reason
             ORDER BY COALESCE(SUM(r.amount), 0) DESC
            """)
    java.util.List<Object[]> aggregateByReason(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query(value = """
            SELECT i.product_id,
                   i.product_name,
                   COALESCE(SUM(i.quantity), 0),
                   COALESCE(SUM(CASE
                       WHEN line_totals.gross_amount > 0 THEN ((i.quantity * i.price) / line_totals.gross_amount) * r.amount
                       ELSE 0
                   END), 0)
              FROM payment_refunds r
              JOIN orders o ON o.id = r.order_id
              JOIN users u ON u.id = o.user_id AND u.role = 'CUSTOMER' AND u.deleted_at IS NULL
              JOIN order_items i ON i.order_id = o.id
              JOIN (
                    SELECT order_id, SUM(quantity * price) AS gross_amount
                      FROM order_items
                     WHERE deleted_at IS NULL
                     GROUP BY order_id
              ) line_totals ON line_totals.order_id = o.id
             WHERE r.status = 'SUCCEEDED'
               AND r.completed_at >= :from
               AND r.completed_at < :to
               AND r.deleted_at IS NULL
               AND o.deleted_at IS NULL
               AND i.deleted_at IS NULL
             GROUP BY i.product_id, i.product_name
             ORDER BY COALESCE(SUM(CASE
                       WHEN line_totals.gross_amount > 0 THEN ((i.quantity * i.price) / line_totals.gross_amount) * r.amount
                       ELSE 0
                   END), 0) DESC
            """, nativeQuery = true)
    java.util.List<Object[]> aggregateRefundByProduct(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM CustomerPaymentRefund r WHERE r.id = :id")
    Optional<CustomerPaymentRefund> findByIdForUpdate(@Param("id") Integer id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE CustomerPaymentRefund r
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
