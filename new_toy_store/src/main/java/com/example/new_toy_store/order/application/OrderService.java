package com.example.new_toy_store.order.application;

import com.example.new_toy_store.order.application.dto.request.OrderRequest;
import com.example.new_toy_store.order.application.dto.response.OrderResponse;
import com.example.new_toy_store.order.domain.Order;
import com.example.new_toy_store.order.domain.OrderRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public OrderResponse create(OrderRequest request) {

        Order order = OrderMapper.toEntity(request);

        repository.save(order);

        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse confirm(Integer id) {
        Order order = getOrder(id);
        order.confirm();
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse ship(Integer id) {
        Order order = getOrder(id);
        order.ship();
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse complete(Integer id) {
        Order order = getOrder(id);
        order.complete();
        return OrderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse cancel(Integer id) {
        Order order = getOrder(id);
        order.cancel();
        return OrderMapper.toResponse(order);
    }

    private Order getOrder(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
}