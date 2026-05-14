package com.example.new_toy_store.order.service;

import com.example.new_toy_store.order.domain.Order;
import com.example.new_toy_store.order.dto.request.OrderRequest;
import com.example.new_toy_store.order.dto.response.OrderResponse;
import com.example.new_toy_store.order.mapper.OrderMapper;
import com.example.new_toy_store.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderResponse createOrder(OrderRequest request) {

        // 1. DTO → Entity
        Order order = OrderMapper.toEntity(request);

        // 2. save DB
        Order savedOrder = orderRepository.save(order);

        // 3. Entity → DTO
        return OrderMapper.toResponse(savedOrder);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }
}