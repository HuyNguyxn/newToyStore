package com.example.new_toy_store.order.application.facade;

import com.example.new_toy_store.order.application.OrderService;
import com.example.new_toy_store.order.application.dto.response.OrderPaymentSnapshot;
import com.example.new_toy_store.order.domain.OrderItem;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderFacade {

    private final OrderService orderService;

    public OrderFacade(OrderService orderService) {
        this.orderService = orderService;
    }

    public OrderItem getCompletedOrderItemForReview(Integer orderItemId, Integer userId) {
        return orderService.getCompletedOrderItemForReview(orderItemId, userId);
    }

    public void verifyOrderOwnership(Integer orderId, Integer userId) {
        orderService.verifyOrderOwnership(orderId, userId);
    }

    public String getOrderStatus(Integer orderId) {
        return orderService.getOrderStatus(orderId);
    }

    public OrderPaymentSnapshot getPaymentSnapshot(Integer orderId) {
        return orderService.getPaymentSnapshot(orderId);
    }

    public boolean isHighRiskCustomer(Integer userId) {
        return orderService.isHighRiskCustomer(userId);
    }

    public void updateOrderRefundStatus(Integer orderId, Map<Integer, Integer> returnedItemsQty) {
        orderService.updateOrderRefundStatus(orderId, returnedItemsQty);
    }
}
