package com.example.new_toy_store.order.api;

import com.example.new_toy_store.order.application.OrderService;
import com.example.new_toy_store.order.application.dto.request.OrderRequest;
import com.example.new_toy_store.order.application.dto.response.OrderResponse;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @PostMapping
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}/confirm")
    public OrderResponse confirm(@PathVariable Integer id) {
        return service.confirm(id);
    }

    @PatchMapping("/{id}/ship")
    public OrderResponse ship(@PathVariable Integer id) {
        return service.ship(id);
    }

    @PatchMapping("/{id}/complete")
    public OrderResponse complete(@PathVariable Integer id) {
        return service.complete(id);
    }

    // CANCEL
    @PatchMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Integer id) {
        return service.cancel(id);
    }
}