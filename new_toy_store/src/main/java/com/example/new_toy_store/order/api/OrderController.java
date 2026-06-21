package com.example.new_toy_store.order.api;

import com.example.new_toy_store.order.application.OrderService;
import com.example.new_toy_store.order.application.dto.request.OrderRequest;
import com.example.new_toy_store.order.application.dto.response.OrderResponse;
import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@Validated
public class OrderController {

    private final OrderService service;
    private final UserRepository userRepository;

    public OrderController(OrderService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @GetMapping("/my-orders")
    public Page<OrderResponse> getUserOrders(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        User user = getAuthenticatedUser(userDetails);
        return service.getUserOrders(user.getId(), pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderDetails(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id) {
        User user = getAuthenticatedUser(userDetails);
        OrderResponse order = service.getOrderDetails(id);

        if (!order.getUserId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
            throw new IllegalArgumentException("Bạn không có quyền xem đơn hàng của người khác");
        }
        return order;
    }

    @PostMapping
    public OrderResponse create(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody OrderRequest request) {
        User user = getAuthenticatedUser(userDetails);
        request.setUserId(user.getId());
        return service.create(request);
    }

    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse confirm(@PathVariable Integer id, @RequestParam(required = false) String note) {
        return service.confirm(id, note);
    }

    @PatchMapping("/{id}/ship")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse ship(@PathVariable Integer id, @RequestParam(required = false) String note) {
        return service.ship(id, note);
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN')")
    public OrderResponse complete(@PathVariable Integer id, @RequestParam(required = false) String note) {
        return service.complete(id, note);
    }

    @PatchMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id, @RequestParam(required = false) String note) {
        User user = getAuthenticatedUser(userDetails);
        OrderResponse order = service.getOrderDetails(id);

        if (!order.getUserId().equals(user.getId()) && !user.getRole().name().equals("ADMIN")) {
            throw new IllegalArgumentException("Bạn không có quyền hủy đơn hàng của người khác");
        }
        return service.cancel(id, note);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    private User getAuthenticatedUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin người dùng"));
    }
}