package com.example.new_toy_store.order.mapper;

import com.example.new_toy_store.order.application.dto.request.OrderRequest;
import com.example.new_toy_store.order.application.dto.response.OrderHistoryResponse;
import com.example.new_toy_store.order.application.dto.response.OrderItemResponse;
import com.example.new_toy_store.order.application.dto.response.OrderResponse;
import com.example.new_toy_store.order.domain.Order;
import com.example.new_toy_store.order.domain.OrderHistory;
import com.example.new_toy_store.order.domain.OrderItem;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static Order toEntity(OrderRequest request) {
        if (request == null) return null;
        return new Order(request.getUserId(), request.getShippingAddress());
    }

    public static OrderResponse toResponse(Order order) {
        if (order == null) return null;

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setShippingAddress(order.getShippingAddress());
        response.setPromoCode(order.getPromoCode());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        if (order.getItems() != null) {
            response.setItems(order.getItems().stream()
                    .map(OrderMapper::toItemResponse)
                    .collect(Collectors.toList()));
        } else {
            response.setItems(Collections.emptyList());
        }

        if (order.getHistories() != null) {
            response.setHistories(order.getHistories().stream()
                    .map(OrderMapper::toHistoryResponse)
                    .collect(Collectors.toList()));
        } else {
            response.setHistories(Collections.emptyList());
        }
        if (order.getStatus() != null) {
            List<String> actions = order.getStatus().getNextValidStates().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList());
            response.setAvailableActions(actions);
        }

        return response;
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