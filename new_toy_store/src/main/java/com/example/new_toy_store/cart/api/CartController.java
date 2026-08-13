package com.example.new_toy_store.cart.api;

import com.example.new_toy_store.cart.application.dto.request.AddCartItemRequest;
import com.example.new_toy_store.cart.application.dto.request.CheckoutCartRequest;
import com.example.new_toy_store.cart.application.dto.request.SyncCartRequest;
import com.example.new_toy_store.cart.application.dto.request.ToggleCartItemSelectionRequest;
import com.example.new_toy_store.cart.application.dto.request.UpdateCartItemQuantityRequest;
import com.example.new_toy_store.cart.application.dto.response.CartResponse;
import com.example.new_toy_store.cart.application.facade.CartFacade;
import com.example.new_toy_store.cart.domain.exception.CartAccessDeniedException;
import com.example.new_toy_store.infrastructure.security.service.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @RequestParam(required = false) String promoCode) {
        verifyOwnerOrAdmin(currentUser, userId, "xem");
        return cartFacade.getCart(userId, promoCode);
    }

    @PostMapping("/{userId}/items")
    public CartResponse addItem(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        verifyOwnerOrAdmin(currentUser, userId, "thêm sản phẩm vào");
        return cartFacade.addItem(userId, request);
    }

    @PostMapping("/{userId}/sync")
    public CartResponse syncCart(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @Valid @RequestBody SyncCartRequest request
    ) {
        verifyOwnerOrAdmin(currentUser, userId, "đồng bộ");
        return cartFacade.syncCart(userId, request);
    }

    @PutMapping("/{userId}/items/{itemId}")
    public CartResponse updateQuantity(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @PathVariable @Positive(message = "ID mục giỏ hàng phải lớn hơn 0") Integer itemId,
            @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {
        verifyOwnerOrAdmin(currentUser, userId, "cập nhật");
        return cartFacade.updateQuantity(userId, itemId, request);
    }

    @PatchMapping("/{userId}/items/{itemId}/toggle")
    public CartResponse toggleItemSelection(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @PathVariable @Positive(message = "ID mục giỏ hàng phải lớn hơn 0") Integer itemId,
            @Valid @RequestBody ToggleCartItemSelectionRequest request
    ) {
        verifyOwnerOrAdmin(currentUser, userId, "thay đổi lựa chọn trong");
        return cartFacade.toggleSelection(userId, itemId, request);
    }

    @DeleteMapping("/{userId}/items/{itemId}")
    public CartResponse removeItem(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @PathVariable @Positive(message = "ID mục giỏ hàng phải lớn hơn 0") Integer itemId
    ) {
        verifyOwnerOrAdmin(currentUser, userId, "xóa sản phẩm khỏi");
        return cartFacade.removeItem(userId, itemId);
    }

    @DeleteMapping("/{userId}")
    public void clearCart(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId
    ) {
        verifyOwnerOrAdmin(currentUser, userId, "xóa");
        cartFacade.clearCart(userId);
    }

    @PostMapping("/{userId}/checkout")
    public com.example.new_toy_store.order.application.dto.response.OrderResponse checkout(
            @AuthenticationPrincipal CustomUserDetails currentUser,
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer userId,
            @Valid @RequestBody CheckoutCartRequest request
    ) {
        verifyOwnerOrAdmin(currentUser, userId, "thanh toán");
        return cartFacade.checkout(userId, request);
    }

    private void verifyOwnerOrAdmin(CustomUserDetails currentUser, Integer targetUserId, String action) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        if (!isAdmin && !currentUser.getId().equals(targetUserId)) {
            throw CartAccessDeniedException.forUserCart(currentUser.getId(), targetUserId, action);
        }
    }
}
