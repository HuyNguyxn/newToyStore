package com.example.new_toy_store.order.api;

import com.example.new_toy_store.order.application.OrderService;
import com.example.new_toy_store.order.application.dto.request.OrderFilterRequest;
import com.example.new_toy_store.order.application.dto.request.OrderRequest;
import com.example.new_toy_store.order.application.dto.request.UpdateShippingRequest;
import com.example.new_toy_store.order.application.dto.response.OrderResponse;
import com.example.new_toy_store.order.domain.exception.OrderAccessDeniedException;
import com.example.new_toy_store.user.application.UserFacade;
import com.example.new_toy_store.user.application.dto.response.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@Validated
public class OrderController {

    private final OrderService service;
    private final UserFacade userFacade;

    public OrderController(OrderService service, UserFacade userFacade) {
        this.service = service;
        this.userFacade = userFacade;
    }

    @GetMapping("/my-orders")
    public Page<OrderResponse> getUserOrders(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        return service.getUserOrders(user.getId(), pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderDetails(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        OrderResponse order = service.getOrderDetails(id);

        if (!order.getUserId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            throw new OrderAccessDeniedException(id, user.getId(), "xem");
        }
        return order;
    }

    @PostMapping
    public OrderResponse create(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody OrderRequest request) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
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
    public OrderResponse cancel(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id,
            @RequestParam(required = false) String note
    ) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        OrderResponse order = service.getOrderDetails(id);

        if (!order.getUserId().equals(user.getId()) && !"ADMIN".equals(user.getRole())) {
            throw new OrderAccessDeniedException(id, user.getId(), "hủy");
        }
        return service.cancel(id, note);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    @GetMapping("/admin/filter")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<OrderResponse> filterOrders(OrderFilterRequest filterRequest, Pageable pageable) {
        return service.filterOrders(filterRequest, pageable);
    }

    @PatchMapping("/{id}/shipping-address")
    public OrderResponse updateShippingAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody UpdateShippingRequest request
    ) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        boolean isAdmin = "ADMIN".equals(user.getRole());
        return service.updateShippingAddress(id, request, user.getId(), isAdmin);
    }

    private UserProfileResponse getAuthenticatedUser(UserDetails userDetails) {
        return userFacade.getCurrentProfile(userDetails.getUsername());
    }
}
