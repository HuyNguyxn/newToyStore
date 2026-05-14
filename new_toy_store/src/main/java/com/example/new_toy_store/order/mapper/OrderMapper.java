package com.example.new_toy_store.order.mapper;

import com.example.new_toy_store.order.domain.Order;
import com.example.new_toy_store.order.domain.OrderItem;
import com.example.new_toy_store.order.dto.request.OrderItemRequest;
import com.example.new_toy_store.order.dto.request.OrderRequest;
import com.example.new_toy_store.order.dto.response.OrderItemResponse;
import com.example.new_toy_store.order.dto.response.OrderResponse;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    //DTO → ENTITY
    public static Order toEntity(OrderRequest request) {

        Order order = new Order("NEW");

        for (OrderItemRequest itemReq : request.getItems()) {

            order.addItem(
                    itemReq.getProductName(),
                    itemReq.getQuantity(),
                    itemReq.getPrice()
            );
        }

        return order;
    }

    //ENTITY → DTO
    public static OrderResponse toResponse(Order order) {

        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(OrderMapper::toItemResponse)
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getCreatedAt(),
                items
        );
    }

    public static OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getProductName(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}