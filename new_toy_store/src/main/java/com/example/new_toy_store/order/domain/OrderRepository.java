package com.example.new_toy_store.order.domain;


import com.example.new_toy_store.order.domain.Order;
import com.example.new_toy_store.order.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCreatedAtBetween(
            java.time.LocalDateTime from,
            java.time.LocalDateTime to
    );
}