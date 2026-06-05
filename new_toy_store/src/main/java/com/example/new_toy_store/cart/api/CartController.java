package com.example.new_toy_store.cart.api;

import com.example.new_toy_store.cart.application.CartService;
import com.example.new_toy_store.cart.application.dto.request.CartItemRequest;
import com.example.new_toy_store.cart.application.dto.response.CartResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
public class CartController {

    private final CartService service;

    public CartController(CartService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public CartResponse getCart(@PathVariable Integer userId) {
        return service.getCartByUserId(userId);
    }

    @PostMapping("/{userId}/items")
    public CartResponse addItem(@PathVariable Integer userId, @Valid @RequestBody CartItemRequest request) {
        return service.addItemToCart(userId, request);
    }

    @PutMapping("/{userId}/items/{itemId}")
    public CartResponse updateQuantity(@PathVariable Integer userId, @PathVariable Integer itemId, @RequestParam int quantity) {
        return service.updateItemQuantity(userId, itemId, quantity);
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public CartResponse removeItem(@PathVariable Integer userId, @PathVariable Integer itemId) {
        return service.removeItemFromCart(userId, itemId);
    }

    @DeleteMapping("/{userId}")
    public void clearCart(@PathVariable Integer userId) {
        service.clearCart(userId);
    }
}