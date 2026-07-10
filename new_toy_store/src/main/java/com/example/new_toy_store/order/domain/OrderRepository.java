package com.example.new_toy_store.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {

    Page<Order> findByUserId(Integer userId, Pageable pageable);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Order findByIdWithItems(@Param("id") Integer id);

    @Query("SELECT COUNT(o) > 0 FROM Order o JOIN o.items i WHERE o.userId = :userId AND i.productId = :productId AND o.status = 'COMPLETED'")
    boolean existsCompletedOrderByUserAndProduct(@Param("userId") Integer userId, @Param("productId") Integer productId);

    boolean existsByUserIdAndPromoCodeAndStatusNot(Integer userId, String promoCode, OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt <= :cutoffTime")
    List<Order> findExpiredOrders(@Param("status") OrderStatus status, @Param("cutoffTime") LocalDateTime cutoffTime);
}