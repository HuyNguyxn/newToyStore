package com.example.new_toy_store.cart.api;

import com.example.new_toy_store.cart.application.facade.CartFacade;
import com.example.new_toy_store.cart.application.dto.request.CartItemRequest;
import com.example.new_toy_store.cart.application.dto.request.CartRequest;
import com.example.new_toy_store.cart.application.dto.request.CheckoutRequest;
import com.example.new_toy_store.cart.application.dto.response.CartResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
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
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @RequestParam(required = false) String promoCode) {
        return cartFacade.getCart(userId, promoCode);
    }

    @PostMapping("/{userId}/items")
    public CartResponse addItem(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @Valid @RequestBody CartItemRequest request
    ) {
        return cartFacade.addItem(userId, request);
    }

    @PostMapping("/{userId}/sync")
    public CartResponse syncCart(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @Valid @RequestBody CartRequest request
    ) {
        return cartFacade.syncCart(userId, request);
    }

    @PutMapping("/{userId}/items/{itemId}")
    public CartResponse updateQuantity(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @PathVariable @Positive(message = "ID mục giỏ hàng phải lớn hơn 0") Integer itemId,
            @RequestParam @Min(value = 1, message = "Số lượng cập nhật phải lớn hơn 0") int quantity) {
        return cartFacade.updateQuantity(userId, itemId, quantity);
    }

    @PatchMapping("/{userId}/items/{itemId}/toggle")
    public CartResponse toggleItemSelection(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @PathVariable @Positive(message = "ID mục giỏ hàng phải lớn hơn 0") Integer itemId,
            @RequestParam boolean isSelected) {
        return cartFacade.toggleSelection(userId, itemId, isSelected);
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
            @Valid @RequestBody CheckoutRequest request) {
        cartFacade.checkout(userId, request);
        return ResponseEntity.ok("Yêu cầu thanh toán đã được tiếp nhận và đang xử lý.");
    }
}
