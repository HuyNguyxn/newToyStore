package com.example.new_toy_store.order.application.facade;

import com.example.new_toy_store.order.application.OrderService;
import com.example.new_toy_store.order.domain.OrderItem;
import org.springframework.stereotype.Component;

@Component
public class OrderFacade {

    private final OrderService orderService;

    public OrderFacade(OrderService orderService) {
        this.orderService = orderService;
    }

    public OrderItem getCompletedOrderItemForReview(Integer orderItemId, Integer userId) {
        return orderService.getCompletedOrderItemForReview(orderItemId, userId);
    }
}
