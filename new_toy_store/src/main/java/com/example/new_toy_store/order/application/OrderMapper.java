package com.example.new_toy_store.order.application;

import com.example.new_toy_store.order.application.dto.request.OrderRequest;
import com.example.new_toy_store.order.application.dto.response.OrderResponse;
import com.example.new_toy_store.order.application.dto.response.OrderItemResponse;
import com.example.new_toy_store.order.domain.Order;

import java.util.stream.Collectors;

public class OrderMapper {

    public static Order toEntity(OrderRequest request) {

        Order order = new Order();

        request.getItems().forEach(i ->
                order.addItem(
                        i.getProductId(),
                        i.getProductName(),
                        i.getQuantity(),
                        i.getPrice()
                )
        );

        return order;
    }

    public static OrderResponse toResponse(Order order) {

        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getItems().stream()
                        .map(i -> new OrderItemResponse(
                                i.getProductName(),
                                i.getQuantity(),
                                i.getPrice()
                        ))
                        .collect(Collectors.toList())
        );
    }
}