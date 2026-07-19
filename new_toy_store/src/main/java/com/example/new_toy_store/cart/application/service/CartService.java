package com.example.new_toy_store.cart.application.service;

import com.example.new_toy_store.cart.application.dto.request.CartItemRequest;
import com.example.new_toy_store.cart.application.dto.request.CartRequest;
import com.example.new_toy_store.cart.domain.Cart;
import com.example.new_toy_store.cart.domain.CartItem;
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
                .orElseThrow(() -> new CartNotFoundException(userId));
    }

    @Transactional
    public Cart addItemToCart(Integer userId, CartItemRequest request, double currentPrice) {
        Cart cart = repository.findByUserId(userId)
                .orElseGet(() -> repository.save(new Cart(userId)));

        cart.addItem(request.getProductId(), request.getVariantId(), request.getQuantity(), currentPrice);
        return repository.save(cart);
    }

    @Transactional
    public Cart syncCart(Integer userId, CartRequest request, Map<Integer, Double> variantPrices) {
        Cart cart = repository.findByUserId(userId)
                .orElseGet(() -> repository.save(new Cart(userId)));

        for (CartItemRequest itemReq : request.getItems()) {
            double price = variantPrices.getOrDefault(itemReq.getVariantId(), 0.0);
            cart.addItem(itemReq.getProductId(), itemReq.getVariantId(), itemReq.getQuantity(), price);
        }
        return repository.save(cart);
    }

    @Transactional
    public Cart updateItemQuantity(Integer userId, Integer itemId, int quantity) {
        Cart cart = repository.findByUserId(userId).orElseThrow(() -> new CartNotFoundException(userId));
        cart.updateItemQuantity(itemId, quantity);
        return repository.save(cart);
    }

    @Transactional
    public Cart toggleItemSelection(Integer userId, Integer itemId, boolean isSelected) {
        Cart cart = repository.findByUserId(userId).orElseThrow(() -> new CartNotFoundException(userId));
        cart.toggleItemSelection(itemId, isSelected);
        return repository.save(cart);
    }

    @Transactional
    public Cart removeItemFromCart(Integer userId, Integer itemId) {
        Cart cart = repository.findByUserId(userId).orElseThrow(() -> new CartNotFoundException(userId));
        cart.removeItem(itemId);
        return repository.save(cart);
    }

    @Transactional
    public void clearCart(Integer userId) {
        repository.findByUserId(userId).ifPresent(cart -> {
            cart.clearCart();
            repository.save(cart);
        });
    }

    @Transactional
    public Cart lockCartForCheckout(Integer userId) {
        Cart cart = repository.findByUserId(userId).orElseThrow(() -> new CartNotFoundException(userId));
        cart.changeStatus(CartStatus.CHECKING_OUT);
        return repository.save(cart);
    }

    @Transactional
    public void clearCheckedOutItems(Integer cartId) {
        Cart cart = repository.findById(cartId).orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));
        cart.getItems().removeIf(CartItem::isSelected);
        cart.changeStatus(CartStatus.ACTIVE);
        repository.save(cart);
    }

    @Transactional
    public void unlockCart(Integer cartId) {
        Cart cart = repository.findById(cartId).orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));
        cart.changeStatus(CartStatus.ACTIVE);
        repository.save(cart);
    }

    @Transactional
    public void syncProductChanges(Integer variantId, double newPrice) {
        itemRepository.updatePriceByVariantId(variantId, newPrice);
    }
}