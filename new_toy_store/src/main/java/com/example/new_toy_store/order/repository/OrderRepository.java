package com.example.new_toy_store.order.repository;

import com.example.new_toy_store.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}