package com.example.new_toy_store.cart.application.service;

import com.example.new_toy_store.cart.application.dto.request.CartItemRequest;
import com.example.new_toy_store.cart.application.dto.request.CartRequest;
import com.example.new_toy_store.cart.domain.Cart;
import com.example.new_toy_store.cart.domain.CartRepository;
import com.example.new_toy_store.cart.domain.CartItemRepository;
import com.example.new_toy_store.cart.domain.CartStatus;
import com.example.new_toy_store.cart.domain.exception.CartNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class CartService {

    private final CartRepository repository;
    private final CartItemRepository itemRepository;

    public CartService(CartRepository repository, CartItemRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public Cart getCartByUserId(Integer userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> CartNotFoundException.byUserId(userId));
    }

    @Transactional
    public Cart addItemToCart(Integer userId, CartItemRequest request, double currentPrice) {
        Cart cart = repository.findForUpdateByUserId(userId)
                .orElseGet(() -> repository.save(new Cart(userId)));

        cart.addItem(request.getProductId(), request.getVariantId(), request.getQuantity(), currentPrice);
        return repository.save(cart);
    }

    @Transactional
    public Cart syncCart(Integer userId, CartRequest request, Map<Integer, Double> variantPrices) {
        Cart cart = repository.findForUpdateByUserId(userId)
                .orElseGet(() -> repository.save(new Cart(userId)));

        for (CartItemRequest itemReq : request.getItems()) {
            double price = variantPrices.getOrDefault(itemReq.getVariantId(), 0.0);
            cart.addItem(itemReq.getProductId(), itemReq.getVariantId(), itemReq.getQuantity(), price);
        }
        return repository.save(cart);
    }

    @Transactional
    public Cart updateItemQuantity(Integer userId, Integer itemId, int quantity) {
        Cart cart = getCartForUpdate(userId);
        cart.updateItemQuantity(itemId, quantity);
        return repository.save(cart);
    }

    @Transactional
    public Cart toggleItemSelection(Integer userId, Integer itemId, boolean isSelected) {
        Cart cart = getCartForUpdate(userId);
        cart.toggleItemSelection(itemId, isSelected);
        return repository.save(cart);
    }

    @Transactional
    public Cart removeItemFromCart(Integer userId, Integer itemId) {
        Cart cart = getCartForUpdate(userId);
        cart.removeItem(itemId);
        return repository.save(cart);
    }

    @Transactional
    public void clearCart(Integer userId) {
        repository.findForUpdateByUserId(userId).ifPresent(cart -> {
            cart.clearCart();
            repository.save(cart);
        });
    }

    @Transactional
    public Cart lockCartForCheckout(Integer userId) {
        Cart cart = getCartForUpdate(userId);
        cart.changeStatus(CartStatus.CHECKING_OUT);
        return repository.save(cart);
    }

    @Transactional
    public void clearCheckedOutItems(Integer cartId) {
        Cart cart = getCartForUpdateById(cartId);
        cart.completeCheckout();
        repository.save(cart);
    }

    @Transactional
    public void unlockCart(Integer cartId) {
        Cart cart = getCartForUpdateById(cartId);
        cart.changeStatus(CartStatus.ACTIVE);
        repository.save(cart);
    }

    @Transactional
    public void syncProductChanges(Integer variantId, double newPrice) {
        repository.touchCartsContainingVariant(variantId);
        itemRepository.updatePriceByVariantId(variantId, newPrice);
    }

    private Cart getCartForUpdate(Integer userId) {
        return repository.findForUpdateByUserId(userId)
                .orElseThrow(() -> CartNotFoundException.byUserId(userId));
    }

    private Cart getCartForUpdateById(Integer cartId) {
        return repository.findForUpdateById(cartId)
                .orElseThrow(() -> CartNotFoundException.byCartId(cartId));
    }
}
