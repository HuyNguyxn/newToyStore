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

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :from AND o.createdAt < :to")
    long countCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT i.productId, i.productName, SUM(i.quantity), SUM(i.quantity * i.price)
              FROM Order o JOIN o.items i
             WHERE o.status IN :statuses
             GROUP BY i.productId, i.productName
             ORDER BY SUM(i.quantity) DESC
            """)
    List<Object[]> findTopSellingProducts(@Param("statuses") List<OrderStatus> statuses, Pageable pageable);

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
