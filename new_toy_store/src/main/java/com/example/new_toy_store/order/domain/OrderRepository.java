package com.example.new_toy_store.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {

    long countByStatus(OrderStatus status);

    @EntityGraph(attributePaths = {"items", "histories"})
    Page<Order> findByUserId(Integer userId, Pageable pageable);

    @EntityGraph(attributePaths = {"items", "histories"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"items", "histories"})
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"items", "histories"})
    Optional<Order> findById(Integer id);

    @EntityGraph(attributePaths = {"items", "histories"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithItemsAndHistories(@Param("id") Integer id);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items i WHERE o.userId = :userId AND i.productId = :productId AND o.status = 'COMPLETED'")
    boolean existsCompletedOrderByUserAndProduct(@Param("userId") Integer userId, @Param("productId") Integer productId);

    boolean existsByUserIdAndPromoCodeAndStatusNot(Integer userId, String promoCode, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt <= :cutoffTime")
    List<Order> findExpiredOrders(@Param("status") OrderStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT i FROM Order o JOIN o.items i WHERE i.id = :orderItemId AND o.userId = :userId AND o.status = 'COMPLETED'")
    Optional<OrderItem> findCompletedOrderItem(@Param("orderItemId") Integer orderItemId, @Param("userId") Integer userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.status IN ('COMPLETED', 'PARTIALLY_REFUNDED', 'FULLY_REFUNDED')")
    long countTotalValidOrders(@Param("userId") Integer userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.status IN ('PARTIALLY_REFUNDED', 'FULLY_REFUNDED')")
    long countRefundedOrders(@Param("userId") Integer userId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status IN :statuses")
    double sumTotalAmountByStatuses(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status IN :statuses AND o.createdAt >= :from AND o.createdAt < :to")
    double sumTotalAmountByStatusesBetween(@Param("statuses") List<OrderStatus> statuses, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :from AND o.createdAt < :to")
    long countCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt >= :from AND o.createdAt < :to")
    long countByStatusBetween(@Param("status") OrderStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses AND o.createdAt >= :from AND o.createdAt < :to")
    long countByStatusesBetween(@Param("statuses") List<OrderStatus> statuses, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COUNT(DISTINCT h.order.id) FROM OrderHistory h WHERE h.status = :status AND h.createdAt >= :from AND h.createdAt < :to")
    long countHistoryByStatusBetween(@Param("status") OrderStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM Order o JOIN o.items i WHERE o.status IN :statuses AND o.createdAt >= :from AND o.createdAt < :to")
    long sumSoldQuantityBetween(@Param("statuses") List<OrderStatus> statuses, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT FUNCTION('date', o.createdAt), COUNT(o), COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.status IN :statuses AND o.createdAt >= :from AND o.createdAt < :to GROUP BY FUNCTION('date', o.createdAt)")
    List<Object[]> aggregateDailyRevenue(@Param("statuses") List<OrderStatus> statuses, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT i.productId, i.productName, SUM(i.quantity), COUNT(DISTINCT o.id), SUM(i.quantity * i.price)
              FROM Order o JOIN o.items i
             WHERE o.status IN :statuses
               AND o.createdAt >= :from
               AND o.createdAt < :to
             GROUP BY i.productId, i.productName
             ORDER BY SUM(i.quantity) DESC
            """)
    List<Object[]> findTopSellingProducts(
            @Param("statuses") List<OrderStatus> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query(value = """
            SELECT c.id, c.name, COALESCE(SUM(i.quantity), 0), COUNT(DISTINCT o.id), COALESCE(SUM(i.quantity * i.price), 0)
              FROM orders o
              JOIN order_items i ON i.order_id = o.id
              JOIN product_categories pc ON pc.product_id = i.product_id
              JOIN categories c ON c.id = pc.category_id
             WHERE o.status IN (:statuses)
               AND o.created_at >= :from
               AND o.created_at < :to
               AND o.deleted_at IS NULL
               AND i.deleted_at IS NULL
             GROUP BY c.id, c.name
             ORDER BY COALESCE(SUM(i.quantity * i.price), 0) DESC
            """, nativeQuery = true)
    List<Object[]> aggregateRevenueByCategory(
            @Param("statuses") List<String> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query("""
            SELECT COALESCE(o.promoCode, 'NO_PROMOTION'), COALESCE(o.promoCode, 'No promotion'), COUNT(o), COALESCE(SUM(o.totalAmount), 0)
              FROM Order o
             WHERE o.status IN :statuses
               AND o.createdAt >= :from
               AND o.createdAt < :to
             GROUP BY o.promoCode
             ORDER BY COALESCE(SUM(o.totalAmount), 0) DESC
            """)
    List<Object[]> aggregateRevenueByPromotion(@Param("statuses") List<OrderStatus> statuses, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query("""
            SELECT o.userId, CONCAT('User #', o.userId), COUNT(o), COALESCE(SUM(o.totalAmount), 0)
              FROM Order o
             WHERE o.status IN :statuses
               AND o.createdAt >= :from
               AND o.createdAt < :to
             GROUP BY o.userId
             ORDER BY COALESCE(SUM(o.totalAmount), 0) DESC
            """)
    List<Object[]> findTopSpendingCustomers(@Param("statuses") List<OrderStatus> statuses, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Order o
               SET o.deletedAt = CURRENT_TIMESTAMP,
                   o.updatedAt = CURRENT_TIMESTAMP,
                   o.version = o.version + 1
             WHERE o.id = :id
               AND o.version = :version
               AND o.status IN :deletableStatuses
            """)
    int softDeleteOrderWithVersion(
            @Param("id") Integer id,
            @Param("version") Long version,
            @Param("deletableStatuses") List<OrderStatus> deletableStatuses
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE OrderItem item
               SET item.deletedAt = CURRENT_TIMESTAMP,
                   item.updatedAt = CURRENT_TIMESTAMP
             WHERE item.order.id = :orderId
            """)
    int softDeleteItemsByOrderId(@Param("orderId") Integer orderId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE OrderHistory history
               SET history.deletedAt = CURRENT_TIMESTAMP,
                   history.updatedAt = CURRENT_TIMESTAMP
             WHERE history.order.id = :orderId
            """)
    int softDeleteHistoriesByOrderId(@Param("orderId") Integer orderId);
}
