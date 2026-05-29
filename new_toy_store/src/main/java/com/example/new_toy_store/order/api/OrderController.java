package com.example.new_toy_store.order.api;

import com.example.new_toy_store.order.application.OrderService;
import com.example.new_toy_store.order.application.dto.request.OrderRequest;
import com.example.new_toy_store.order.application.dto.response.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping("/user/{userId}")
    public Page<OrderResponse> getUserOrders(@PathVariable Integer userId, Pageable pageable) {
        return service.getUserOrders(userId, pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderDetails(@PathVariable Integer id) {
        return service.getOrderDetails(id);
    }

    @PostMapping
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}/confirm")
    public OrderResponse confirm(@PathVariable Integer id, @RequestParam(required = false) String note) {
        return service.confirm(id, note);
    }

    @PatchMapping("/{id}/ship")
    public OrderResponse ship(@PathVariable Integer id, @RequestParam(required = false) String note) {
        return service.ship(id, note);
    }

    @PatchMapping("/{id}/complete")
    public OrderResponse complete(@PathVariable Integer id, @RequestParam(required = false) String note) {
        return service.complete(id, note);
    }

    @PatchMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Integer id, @RequestParam(required = false) String note) {
        return service.cancel(id, note);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}