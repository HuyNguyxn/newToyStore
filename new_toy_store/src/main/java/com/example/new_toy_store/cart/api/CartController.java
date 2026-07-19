package com.example.new_toy_store.cart.api;

import com.example.new_toy_store.cart.application.facade.CartFacade;
import com.example.new_toy_store.cart.application.dto.request.CartItemRequest;
import com.example.new_toy_store.cart.application.dto.request.CartRequest;
import com.example.new_toy_store.cart.application.dto.request.CheckoutRequest;
import com.example.new_toy_store.cart.application.dto.response.CartResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@Validated
public class CartController {

    private final CartFacade cartFacade;

    public CartController(CartFacade cartFacade) {
        this.cartFacade = cartFacade;
    }

    @GetMapping("/{userId}")
    public CartResponse getCart(
            @PathVariable Integer userId,
            @RequestParam(required = false) String promoCode) {
        return cartFacade.getCart(userId, promoCode);
    }

    @PostMapping("/{userId}/items")
    public CartResponse addItem(@PathVariable Integer userId, @Valid @RequestBody CartItemRequest request) {
        return cartFacade.addItem(userId, request);
    }

    @PostMapping("/{userId}/sync")
    public CartResponse syncCart(@PathVariable Integer userId, @Valid @RequestBody CartRequest request) {
        return cartFacade.syncCart(userId, request);
    }

    @PutMapping("/{userId}/items/{itemId}")
    public CartResponse updateQuantity(
            @PathVariable Integer userId,
            @PathVariable Integer itemId,
            @RequestParam @Min(value = 1, message = "Số lượng cập nhật phải lớn hơn 0") int quantity) {
        return cartFacade.updateQuantity(userId, itemId, quantity);
    }

    @PatchMapping("/{userId}/items/{itemId}/toggle")
    public CartResponse toggleItemSelection(
            @PathVariable Integer userId,
            @PathVariable Integer itemId,
            @RequestParam boolean isSelected) {
        return cartFacade.toggleSelection(userId, itemId, isSelected);
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public CartResponse removeItem(@PathVariable Integer userId, @PathVariable Integer itemId) {
        return cartFacade.removeItem(userId, itemId);
    }

    @DeleteMapping("/{userId}")
    public void clearCart(@PathVariable Integer userId) {
        cartFacade.clearCart(userId);
    }

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<String> checkout(
            @PathVariable Integer userId,
            @Valid @RequestBody CheckoutRequest request) {
        cartFacade.checkout(userId, request);
        return ResponseEntity.ok("Yêu cầu thanh toán đã được tiếp nhận và đang xử lý.");
    }
}