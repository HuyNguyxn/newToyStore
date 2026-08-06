package com.example.new_toy_store.order.application.listener;

import com.example.new_toy_store.global.event.CartCheckoutRequestedEvent;
import com.example.new_toy_store.order.application.OrderService;
import com.example.new_toy_store.order.application.dto.request.OrderItemRequest;
import com.example.new_toy_store.order.application.dto.request.OrderRequest;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartCheckoutRequestedEventListener {

    private final OrderService orderService;

    public CartCheckoutRequestedEventListener(OrderService orderService) {
        this.orderService = orderService;
    }

    @EventListener
    public void handleCartCheckoutRequested(CartCheckoutRequestedEvent event) {
        OrderRequest request = new OrderRequest();
        request.setUserId(event.getUserId());
        request.setShippingAddress(event.getShippingAddress());
        request.setPromoCode(event.getPromoCode());

        List<OrderItemRequest> items = event.getItems().stream()
                .map(item -> {
                    OrderItemRequest req = new OrderItemRequest();
                    req.setProductId(item.getProductId());
                    req.setVariantId(item.getVariantId());
                    req.setQuantity(item.getQuantity());
                    return req;
                })
                .collect(Collectors.toList());

        request.setItems(items);

        orderService.create(request, event.getCartId());
    }
}
