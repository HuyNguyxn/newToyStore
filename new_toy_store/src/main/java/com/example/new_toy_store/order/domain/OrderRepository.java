package com.example.new_toy_store.order.domain;

import com.example.new_toy_store.user.domain.User;
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

    @Query("""
            SELECT COUNT(o)
              FROM Order o JOIN User u ON o.userId = u.id
             WHERE o.status = :status
               AND (u.role IS NULL OR u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER)
            """)
    long countByStatus(@Param("status") OrderStatus status);

    @EntityGraph(attributePaths = {"items"})
    Page<Order> findByUserId(Integer userId, Pageable pageable);

    @EntityGraph(attributePaths = {"items"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"items"})
    Page<Order> findAll(Specification<Order> spec, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"items"})
    Optional<Order> findById(Integer id);

    @EntityGraph(attributePaths = {"items"})
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithItemsAndHistories(@Param("id") Integer id);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items i WHERE o.userId = :userId AND i.productId = :productId AND o.status IN ('COMPLETED', 'DELIVERED', 'SHIPPED')")
    boolean existsCompletedOrderByUserAndProduct(@Param("userId") Integer userId, @Param("productId") Integer productId);

    boolean existsByUserIdAndPromoCodeAndStatusNot(Integer userId, String promoCode, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt <= :cutoffTime")
    List<Order> findExpiredOrders(@Param("status") OrderStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT i FROM Order o JOIN o.items i WHERE i.id = :orderItemId AND o.userId = :userId AND o.status IN ('COMPLETED', 'DELIVERED', 'SHIPPED')")
    Optional<OrderItem> findCompletedOrderItem(@Param("orderItemId") Integer orderItemId, @Param("userId") Integer userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.status IN ('COMPLETED', 'PARTIALLY_REFUNDED', 'FULLY_REFUNDED')")
    long countTotalValidOrders(@Param("userId") Integer userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.userId = :userId AND o.status IN ('PARTIALLY_REFUNDED', 'FULLY_REFUNDED')")
    long countRefundedOrders(@Param("userId") Integer userId);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0)
              FROM Order o JOIN User u ON o.userId = u.id
             WHERE o.status IN :statuses
               AND (u.role IS NULL OR u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER)
            """)
    double sumTotalAmountByStatuses(@Param("statuses") List<OrderStatus> statuses);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0)
              FROM Order o JOIN User u ON o.userId = u.id
             WHERE o.status IN :statuses
               AND (u.role IS NULL OR u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER)
               AND EXISTS (
                   SELECT h.id FROM OrderHistory h
                    WHERE h.order = o
                      AND h.status = com.example.new_toy_store.order.domain.OrderStatus.COMPLETED
                      AND h.createdAt >= :from
                      AND h.createdAt < :to
               )
            """)
    double sumTotalAmountByStatusesBetween(@Param("statuses") List<OrderStatus> statuses, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(o)
              FROM Order o JOIN User u ON o.userId = u.id
             WHERE o.createdAt >= :from
               AND o.createdAt < :to
               AND (u.role IS NULL OR u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER)
            """)
    long countCreatedBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(o)
              FROM Order o JOIN User u ON o.userId = u.id
             WHERE o.status = :status
               AND o.createdAt >= :from
               AND o.createdAt < :to
               AND (u.role IS NULL OR u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER)
            """)
    long countByStatusBetween(@Param("status") OrderStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(o)
              FROM Order o JOIN User u ON o.userId = u.id
             WHERE o.status IN :statuses
               AND o.createdAt >= :from
               AND o.createdAt < :to
               AND (u.role IS NULL OR u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER)
            """)
    long countByStatusesBetween(@Param("statuses") List<OrderStatus> statuses, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COUNT(DISTINCT h.order.id)
              FROM OrderHistory h JOIN User u ON h.order.userId = u.id
             WHERE h.status = :status
               AND h.createdAt >= :from
               AND h.createdAt < :to
               AND (u.role IS NULL OR u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER)
            """)
    long countHistoryByStatusBetween(@Param("status") OrderStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("""
            SELECT COALESCE(SUM(i.quantity), 0)
              FROM Order o JOIN o.items i JOIN User u ON o.userId = u.id
             WHERE o.status IN :statuses
               AND (u.role IS NULL OR u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER)
               AND EXISTS (
                   SELECT h.id FROM OrderHistory h
                    WHERE h.order = o
                      AND h.status = com.example.new_toy_store.order.domain.OrderStatus.COMPLETED
                      AND h.createdAt >= :from
                      AND h.createdAt < :to
               )
            """)
    long sumSoldQuantityBetween(@Param("statuses") List<OrderStatus> statuses, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT DATE(completed.completed_at), COUNT(o.id), COALESCE(SUM(o.total_amount), 0)
              FROM orders o
              JOIN users u ON u.id = o.user_id
                          AND (u.role IS NULL OR u.role = 'CUSTOMER')
                          AND u.deleted_at IS NULL
              JOIN (
                    SELECT order_id, MIN(created_at) AS completed_at
                      FROM order_histories
                     WHERE status = 'COMPLETED'
                       AND deleted_at IS NULL
                     GROUP BY order_id
              ) completed ON completed.order_id = o.id
             WHERE o.status IN (:statuses)
               AND completed.completed_at >= :from
               AND completed.completed_at < :to
               AND o.deleted_at IS NULL
             GROUP BY DATE(completed.completed_at)
            """, nativeQuery = true)
    List<Object[]> aggregateDailyRevenue(@Param("statuses") List<String> statuses, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT DATE(o.created_at), COUNT(o.id)
              FROM orders o
              JOIN users u ON u.id = o.user_id
                          AND (u.role IS NULL OR u.role = 'CUSTOMER')
                          AND u.deleted_at IS NULL
             WHERE o.created_at >= :from
               AND o.created_at < :to
               AND o.deleted_at IS NULL
             GROUP BY DATE(o.created_at)
            """, nativeQuery = true)
    List<Object[]> aggregateDailyCreatedOrders(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT DATE(completed.completed_at),
                   COALESCE(SUM(i.quantity), 0),
                   COALESCE(SUM(i.quantity * COALESCE(NULLIF(i.cost_price_snapshot, 0), pv.cost_price, 0)), 0)
              FROM orders o
              JOIN users u ON u.id = o.user_id
                         AND (u.role IS NULL OR u.role = 'CUSTOMER')
                         AND u.deleted_at IS NULL
              JOIN order_items i ON i.order_id = o.id AND i.deleted_at IS NULL
              JOIN (
                    SELECT order_id, MIN(created_at) AS completed_at
                      FROM order_histories
                     WHERE status = 'COMPLETED'
                       AND deleted_at IS NULL
                     GROUP BY order_id
              ) completed ON completed.order_id = o.id
              LEFT JOIN product_variants pv ON pv.id = i.variant_id AND pv.deleted_at IS NULL
             WHERE o.status IN (:statuses)
               AND completed.completed_at >= :from
               AND completed.completed_at < :to
               AND o.deleted_at IS NULL
             GROUP BY DATE(completed.completed_at)
            """, nativeQuery = true)
    List<Object[]> aggregateDailyCostAndSoldQuantity(
            @Param("statuses") List<String> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    @Query("""
            SELECT i.productId, i.productName, SUM(i.quantity), COUNT(DISTINCT o.id), SUM(i.quantity * i.price)
              FROM Order o JOIN o.items i JOIN User u ON o.userId = u.id
             WHERE o.status IN :statuses
               AND (u.role IS NULL OR u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER)
               AND EXISTS (
                   SELECT h.id FROM OrderHistory h
                    WHERE h.order = o
                      AND h.status = com.example.new_toy_store.order.domain.OrderStatus.COMPLETED
                      AND h.createdAt >= :from
                      AND h.createdAt < :to
               )
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
            SELECT p.id,
                   p.name,
                    COALESCE(SUM(CASE WHEN completed.order_id IS NOT NULL AND u.id IS NOT NULL THEN i.quantity ELSE 0 END), 0) AS sold_qty,
                    COUNT(DISTINCT CASE WHEN completed.order_id IS NOT NULL AND u.id IS NOT NULL THEN o.id END) AS order_cnt,
                    COALESCE(SUM(CASE WHEN completed.order_id IS NOT NULL AND u.id IS NOT NULL THEN i.quantity * i.price ELSE 0 END), 0) AS total_rev
              FROM products p
              LEFT JOIN order_items i ON i.product_id = p.id AND i.deleted_at IS NULL
              LEFT JOIN orders o ON o.id = i.order_id AND o.status IN (:statuses) AND o.deleted_at IS NULL
               LEFT JOIN (
                     SELECT order_id, MIN(created_at) AS completed_at
                       FROM order_histories
                      WHERE status = 'COMPLETED'
                        AND created_at >= :from
                        AND created_at < :to
                        AND deleted_at IS NULL
                      GROUP BY order_id
               ) completed ON completed.order_id = o.id
              LEFT JOIN users u ON u.id = o.user_id AND (u.role IS NULL OR u.role = 'CUSTOMER') AND u.deleted_at IS NULL
             WHERE p.deleted_at IS NULL
               AND p.status = 'ACTIVE'
             GROUP BY p.id, p.name
             HAVING COALESCE(SUM(CASE WHEN completed.order_id IS NOT NULL AND u.id IS NOT NULL THEN i.quantity ELSE 0 END), 0) <= :maxUnits
             ORDER BY sold_qty ASC, p.id DESC
            """, nativeQuery = true)
    List<Object[]> findSlowSellingProducts(
            @Param("statuses") List<String> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("maxUnits") int maxUnits,
            Pageable pageable
    );

    @Query(value = """
            SELECT COUNT(*)
              FROM (
                    SELECT p.id
                      FROM products p
                      LEFT JOIN order_items i ON i.product_id = p.id AND i.deleted_at IS NULL
                      LEFT JOIN orders o ON o.id = i.order_id AND o.status IN (:statuses) AND o.deleted_at IS NULL
                       LEFT JOIN (
                             SELECT order_id, MIN(created_at) AS completed_at
                               FROM order_histories
                              WHERE status = 'COMPLETED'
                                AND created_at >= :from
                                AND created_at < :to
                                AND deleted_at IS NULL
                              GROUP BY order_id
                       ) completed ON completed.order_id = o.id
                      LEFT JOIN users u ON u.id = o.user_id
                                       AND (u.role IS NULL OR u.role = 'CUSTOMER')
                                       AND u.deleted_at IS NULL
                     WHERE p.deleted_at IS NULL
                       AND p.status = 'ACTIVE'
                     GROUP BY p.id
                     HAVING COALESCE(SUM(CASE WHEN completed.order_id IS NOT NULL AND u.id IS NOT NULL THEN i.quantity ELSE 0 END), 0) <= :maxUnits
                   ) slow_products
            """, nativeQuery = true)
    long countSlowSellingProducts(
            @Param("statuses") List<String> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("maxUnits") int maxUnits
    );

    @Query(value = """
            SELECT prod_root.root_id,
                   prod_root.root_name,
                   COALESCE(SUM(i.quantity), 0),
                   COALESCE(SUM(
                       CASE
                           WHEN line_totals.gross_amount > 0
                           THEN ((i.quantity * i.price) / line_totals.gross_amount) * o.total_amount
                           ELSE 0
                       END
                   ), 0)
              FROM orders o
               JOIN (
                     SELECT order_id, MIN(created_at) AS completed_at
                       FROM order_histories
                      WHERE status = 'COMPLETED'
                        AND deleted_at IS NULL
                      GROUP BY order_id
               ) completed ON completed.order_id = o.id
              JOIN users u ON u.id = o.user_id AND (u.role IS NULL OR u.role = 'CUSTOMER') AND u.deleted_at IS NULL
              JOIN order_items i ON i.order_id = o.id
              JOIN (
                    SELECT order_id, SUM(quantity * price) AS gross_amount
                      FROM order_items
                     WHERE deleted_at IS NULL
                     GROUP BY order_id
              ) line_totals ON line_totals.order_id = o.id
              JOIN (
                    SELECT pc.product_id,
                           MIN(COALESCE(p2.id, p1.id, c.id)) AS root_id,
                           MIN(COALESCE(p2.name, p1.name, c.name)) AS root_name
                      FROM product_categories pc
                      JOIN categories c ON c.id = pc.category_id AND c.deleted_at IS NULL
                      LEFT JOIN categories p1 ON p1.id = c.parent_id AND p1.deleted_at IS NULL
                      LEFT JOIN categories p2 ON p2.id = p1.parent_id AND p2.deleted_at IS NULL
                     GROUP BY pc.product_id
              ) prod_root ON prod_root.product_id = i.product_id
             WHERE o.status IN (:statuses)
               AND completed.completed_at >= :from
               AND completed.completed_at < :to
               AND o.deleted_at IS NULL
               AND i.deleted_at IS NULL
             GROUP BY prod_root.root_id, prod_root.root_name
             ORDER BY COALESCE(SUM(
                       CASE
                           WHEN line_totals.gross_amount > 0
                           THEN ((i.quantity * i.price) / line_totals.gross_amount) * o.total_amount
                           ELSE 0
                       END
                   ), 0) DESC
            """, nativeQuery = true)
    List<Object[]> aggregateRevenueByCategory(
            @Param("statuses") List<String> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

    @Query(value = """
            SELECT o.user_id,
                   u.full_name,
                   COUNT(o.id) AS order_count,
                   COALESCE(SUM(item_totals.purchased_quantity), 0) AS purchased_quantity,
                   COALESCE(SUM(o.total_amount), 0) AS total_spent
              FROM orders o
               JOIN (
                     SELECT order_id, MIN(created_at) AS completed_at
                       FROM order_histories
                      WHERE status = 'COMPLETED'
                        AND deleted_at IS NULL
                      GROUP BY order_id
               ) completed ON completed.order_id = o.id
              JOIN users u ON u.id = o.user_id
              LEFT JOIN (
                    SELECT oi.order_id, SUM(oi.quantity) AS purchased_quantity
                      FROM order_items oi
                     WHERE oi.deleted_at IS NULL
                     GROUP BY oi.order_id
              ) item_totals ON item_totals.order_id = o.id
             WHERE o.status IN (:statuses)
               AND completed.completed_at >= :from
               AND completed.completed_at < :to
               AND o.deleted_at IS NULL
               AND u.deleted_at IS NULL
               AND (u.role IS NULL OR u.role = 'CUSTOMER')
             GROUP BY o.user_id, u.full_name
             ORDER BY total_spent DESC
            """, nativeQuery = true)
    List<Object[]> findTopSpendingCustomers(@Param("statuses") List<String> statuses, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to, Pageable pageable);

    @Query("""
            SELECT FUNCTION('date', u.createdAt), CONCAT(FUNCTION('date', u.createdAt)), COUNT(u), 0
              FROM User u
             WHERE u.createdAt >= :from
               AND u.createdAt < :to
               AND (u.role IS NULL OR u.role = com.example.new_toy_store.user.domain.UserRole.CUSTOMER)
             GROUP BY FUNCTION('date', u.createdAt)
             ORDER BY FUNCTION('date', u.createdAt)
            """)
    List<Object[]> aggregateNewCustomerTrend(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT 'NEW_CUSTOMERS', 'Khách hàng mới', COUNT(*), 0
              FROM users u
             WHERE u.created_at >= :from
               AND u.created_at < :to
               AND (u.role IS NULL OR u.role = 'CUSTOMER')
               AND u.deleted_at IS NULL
            UNION ALL
            SELECT 'ORDERING_CUSTOMERS', 'Khách hàng đã đặt hàng', COUNT(DISTINCT o.user_id), 0
              FROM orders o
              JOIN users u ON u.id = o.user_id AND (u.role IS NULL OR u.role = 'CUSTOMER') AND u.deleted_at IS NULL
             WHERE o.created_at >= :from
               AND o.created_at < :to
               AND o.deleted_at IS NULL
            UNION ALL
            SELECT 'REPEAT_CUSTOMERS', 'Khách hàng quay lại', COUNT(*), 0
              FROM (
                    SELECT o.user_id
                      FROM orders o
                      JOIN users u ON u.id = o.user_id AND (u.role IS NULL OR u.role = 'CUSTOMER') AND u.deleted_at IS NULL
                     WHERE o.created_at >= :from
                       AND o.created_at < :to
                       AND o.deleted_at IS NULL
                     GROUP BY o.user_id
                    HAVING COUNT(*) >= 2
                   ) repeat_users
            """, nativeQuery = true)
    List<Object[]> aggregateCustomerSummary(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query(value = """
            SELECT i.product_id,
                   i.product_name,
                   COALESCE(SUM(i.quantity), 0),
                   COALESCE(SUM(
                       CASE
                           WHEN order_totals.gross_amount > 0
                           THEN ((i.quantity * i.price) / order_totals.gross_amount) * o.total_amount
                           ELSE 0
                       END
                   ), 0),
                   MAX(COALESCE(refunds.refund_amount, 0)),
                   COALESCE(SUM(i.quantity * COALESCE(NULLIF(i.cost_price_snapshot, 0), pv.cost_price, 0)), 0)
              FROM orders o
               JOIN (
                     SELECT order_id, MIN(created_at) AS completed_at
                       FROM order_histories
                      WHERE status = 'COMPLETED'
                        AND deleted_at IS NULL
                      GROUP BY order_id
               ) completed ON completed.order_id = o.id
              JOIN users u ON u.id = o.user_id AND (u.role IS NULL OR u.role = 'CUSTOMER') AND u.deleted_at IS NULL
              JOIN order_items i ON i.order_id = o.id
              JOIN (
                    SELECT order_id, SUM(quantity * price) AS gross_amount
                      FROM order_items
                     WHERE deleted_at IS NULL
                     GROUP BY order_id
              ) order_totals ON order_totals.order_id = o.id
              LEFT JOIN product_variants pv ON pv.id = i.variant_id
              LEFT JOIN (
                    SELECT ri.product_id,
                           COALESCE(SUM(
                                CASE
                                    WHEN line_totals.gross_amount > 0
                                    THEN ((ri.quantity * ri.price) / line_totals.gross_amount) * pr.amount
                                    ELSE 0
                                END
                            ), 0) AS refund_amount
                      FROM payment_refunds pr
                      JOIN orders ro ON ro.id = pr.order_id
                      JOIN users ru ON ru.id = ro.user_id AND (ru.role IS NULL OR ru.role = 'CUSTOMER') AND ru.deleted_at IS NULL
                      JOIN order_items ri ON ri.order_id = ro.id
                      JOIN (
                            SELECT order_id, SUM(quantity * price) AS gross_amount
                              FROM order_items
                             WHERE deleted_at IS NULL
                             GROUP BY order_id
                      ) line_totals ON line_totals.order_id = ro.id
                     WHERE pr.status = 'SUCCEEDED'
                       AND pr.completed_at >= :from
                       AND pr.completed_at < :to
                       AND pr.deleted_at IS NULL
                       AND ro.deleted_at IS NULL
                       AND ri.deleted_at IS NULL
                     GROUP BY ri.product_id
             ) refunds ON refunds.product_id = i.product_id
             WHERE o.status IN (:statuses)
               AND completed.completed_at >= :from
               AND completed.completed_at < :to
               AND o.deleted_at IS NULL
               AND i.deleted_at IS NULL
             GROUP BY i.product_id, i.product_name
             ORDER BY (
                    COALESCE(SUM(
                        CASE
                            WHEN order_totals.gross_amount > 0
                            THEN ((i.quantity * i.price) / order_totals.gross_amount) * o.total_amount
                            ELSE 0
                        END
                    ), 0)
                    - MAX(COALESCE(refunds.refund_amount, 0))
                    - COALESCE(SUM(i.quantity * COALESCE(NULLIF(i.cost_price_snapshot, 0), pv.cost_price, 0)), 0)
             ) DESC
            """, nativeQuery = true)
    List<Object[]> aggregateProfitMarginByProduct(
            @Param("statuses") List<String> statuses,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable
    );

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
