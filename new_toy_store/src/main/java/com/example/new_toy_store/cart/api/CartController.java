package com.example.new_toy_store.cart.api;

import com.example.new_toy_store.cart.application.dto.request.AddCartItemRequest;
import com.example.new_toy_store.cart.application.dto.request.CheckoutCartRequest;
import com.example.new_toy_store.cart.application.dto.request.SyncCartRequest;
import com.example.new_toy_store.cart.application.dto.request.ToggleCartItemSelectionRequest;
import com.example.new_toy_store.cart.application.dto.request.UpdateCartItemQuantityRequest;
import com.example.new_toy_store.cart.application.dto.response.CartResponse;
import com.example.new_toy_store.cart.application.facade.CartFacade;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @RequestParam(required = false) String promoCode) {
        return cartFacade.getCart(userId, promoCode);
    }

    @PostMapping("/{userId}/items")
    public CartResponse addItem(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return cartFacade.addItem(userId, request);
    }

    @PostMapping("/{userId}/sync")
    public CartResponse syncCart(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @Valid @RequestBody SyncCartRequest request
    ) {
        return cartFacade.syncCart(userId, request);
    }

    @PutMapping("/{userId}/items/{itemId}")
    public CartResponse updateQuantity(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @PathVariable @Positive(message = "ID mục giỏ hàng phải lớn hơn 0") Integer itemId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {
        return cartFacade.updateQuantity(userId, itemId, request);
    }

    @PatchMapping("/{userId}/items/{itemId}/toggle")
    public CartResponse toggleItemSelection(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @PathVariable @Positive(message = "ID mục giỏ hàng phải lớn hơn 0") Integer itemId,
            @Valid @RequestBody ToggleCartItemSelectionRequest request
    ) {
        return cartFacade.toggleSelection(userId, itemId, request);
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public CartResponse removeItem(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @PathVariable @Positive(message = "ID mục giỏ hàng phải lớn hơn 0") Integer itemId
    ) {
        return cartFacade.removeItem(userId, itemId);
    }

    @DeleteMapping("/{userId}")
    public void clearCart(@PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId) {
        cartFacade.clearCart(userId);
    }

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<String> checkout(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @Valid @RequestBody CheckoutCartRequest request
    ) {
        cartFacade.checkout(userId, request);
        return ResponseEntity.ok("Yêu cầu thanh toán đã được tiếp nhận và đang xử lý.");
    }
}
