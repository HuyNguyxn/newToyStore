package com.example.new_toy_store.order.mapper;

import com.example.new_toy_store.order.application.dto.request.OrderRequest;
import com.example.new_toy_store.order.application.dto.response.OrderHistoryResponse;
import com.example.new_toy_store.order.application.dto.response.OrderItemResponse;
import com.example.new_toy_store.order.application.dto.response.OrderResponse;
import com.example.new_toy_store.order.domain.Order;
import com.example.new_toy_store.order.domain.OrderHistory;
import com.example.new_toy_store.order.domain.OrderItem;
import com.example.new_toy_store.order.domain.OrderStatus;

import java.util.List;

public final class OrderMapper {

    private OrderMapper() {}

    public static Order toEntity(OrderRequest request) {
        if (request == null) return null;
        return new Order(request.getUserId(), request.getShippingAddress());
    }

    public static OrderResponse toResponse(Order order) {
        if (order == null) return null;

        OrderResponse response = new OrderResponse();
        mapOrderFields(order, response);
        response.setItems(toItemResponses(order));
        response.setHistories(toHistoryResponses(order));
        response.setAvailableActions(toAvailableActionCodes(order.getStatus()));
        response.setAllowedNextActions(toAllowedNextActions(order.getStatus()));
        return response;
    }

    private static void mapOrderFields(Order order, OrderResponse response) {
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setShippingAddress(order.getShippingAddress());
        response.setPromoCode(order.getPromoCode());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
    }

    private static List<OrderItemResponse> toItemResponses(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return List.of();
        }

        return order.getItems().stream()
                .map(OrderMapper::toItemResponse)
                .toList();
    }

    private static List<OrderHistoryResponse> toHistoryResponses(Order order) {
        if (order.getHistories() == null || order.getHistories().isEmpty()) {
            return List.of();
        }

        return order.getHistories().stream()
                .map(OrderMapper::toHistoryResponse)
                .toList();
    }

    private static List<String> toAvailableActionCodes(OrderStatus status) {
        return toAllowedNextActions(status).stream()
                .map(OrderStatus::name)
                .toList();
    }

    private static List<OrderStatus> toAllowedNextActions(OrderStatus status) {
        if (status == null) {
            return List.of();
        }
        return status.getNextValidStates();
    }

    private static OrderItemResponse toItemResponse(OrderItem item) {
        if (item == null) return null;

        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setVariantId(item.getVariantId());
        response.setProductName(item.getProductName());
        response.setVariantAttributesSnapshot(item.getVariantAttributesSnapshot());
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());
        return response;
    }

    private static OrderHistoryResponse toHistoryResponse(OrderHistory history) {
        if (history == null) return null;

        OrderHistoryResponse response = new OrderHistoryResponse();
        response.setId(history.getId());
        response.setStatus(history.getStatus());
        response.setNote(history.getNote());
        response.setCreatedAt(history.getCreatedAt());
        return response;
    }
}
