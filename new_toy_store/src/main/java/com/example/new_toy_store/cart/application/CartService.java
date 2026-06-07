package com.example.new_toy_store.cart.application;

import com.example.new_toy_store.cart.application.dto.request.CartItemRequest;
import com.example.new_toy_store.cart.application.dto.response.CartResponse;
import com.example.new_toy_store.cart.domain.Cart;
import com.example.new_toy_store.cart.domain.CartItem;
import com.example.new_toy_store.cart.domain.CartRepository;
import com.example.new_toy_store.cart.mapper.CartMapper;
import com.example.new_toy_store.product.application.ProductService;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductVariant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository repository;
    private final ProductService productService;

    public CartService(CartRepository repository, ProductService productService) {
        this.repository = repository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(Integer userId) {
        Cart cart = repository.findByUserId(userId).orElse(new Cart(userId));
        return enrichCartData(cart);
    }

    @Transactional
    public CartResponse addItemToCart(Integer userId, CartItemRequest request) {
        Cart cart = repository.findByUserId(userId).orElseGet(() -> new Cart(userId));

        validateStock(request.getProductId(), request.getVariantId(), request.getQuantity(), cart);

        cart.addItem(request.getProductId(), request.getVariantId(), request.getQuantity());
        repository.save(cart);

        return enrichCartData(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(Integer userId, Integer itemId, int quantity) {
        Cart cart = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item not found"));

        validateStock(item.getProductId(), item.getVariantId(), quantity, null);

        cart.updateItemQuantity(itemId, quantity);
        repository.save(cart);

        return enrichCartData(cart);
    }

    @Transactional
    public CartResponse removeItemFromCart(Integer userId, Integer itemId) {
        Cart cart = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.removeItem(itemId);
        repository.save(cart);

        return enrichCartData(cart);
    }

    @Transactional
    public void clearCart(Integer userId) {
        Cart cart = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cart.clearCart();
        repository.save(cart);
    }

    private void validateStock(Integer productId, Integer variantId, int requestedQuantity, Cart cart) {
        Product product = productService.getProductEntity(productId);
        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Variant not found"));

        int currentQuantityInCart = 0;
        if (cart != null) {
            currentQuantityInCart = cart.getItems().stream()
                    .filter(i -> i.getVariantId().equals(variantId))
                    .mapToInt(CartItem::getQuantity)
                    .sum();
        }

        if (variant.getInventory().getStockQuantity() < (currentQuantityInCart + requestedQuantity)) {
            throw new RuntimeException("Insufficient stock to add to cart");
        }
    }

    private CartResponse enrichCartData(Cart cart) {
        if (cart.getItems().isEmpty()) {
            return CartMapper.toResponse(cart, Map.of());
        }

        Set<Integer> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());

        Map<Integer, Product> productMap = productService.getProductsByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return CartMapper.toResponse(cart, productMap);
    }
}