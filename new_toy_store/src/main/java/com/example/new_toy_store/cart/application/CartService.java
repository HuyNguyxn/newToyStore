package com.example.new_toy_store.cart.application;

import com.example.new_toy_store.cart.application.dto.request.CartItemRequest;
import com.example.new_toy_store.cart.application.dto.request.CartRequest;
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
        return getCartData(cart);
    }

    @Transactional
    public CartResponse addItemToCart(Integer userId, CartItemRequest request) {
        Cart cart = repository.findByUserId(userId).orElseGet(() -> new Cart(userId));
        Product product = productService.getProductEntity(request.getProductId());

        int currentQuantityInCart = cart.getItems().stream()
                .filter(item -> item.getVariantId().equals(request.getVariantId()))
                .mapToInt(CartItem::getQuantity)
                .sum();

        int finalTargetQuantity = currentQuantityInCart + request.getQuantity();

        checkStockSufficiency(product, request.getVariantId(), finalTargetQuantity);

        cart.addItem(request.getProductId(), request.getVariantId(), request.getQuantity());
        repository.save(cart);
        return getCartData(cart);
    }

    @Transactional
    public CartResponse syncCart(Integer userId, CartRequest request) {
        Cart cart = repository.findByUserId(userId).orElseGet(() -> new Cart(userId));

        for (CartItemRequest itemReq : request.getItems()) {
            try {
                Product product = productService.getProductEntity(itemReq.getProductId());

                if (!product.isAvailableForPurchase()) {
                    continue;
                }

                ProductVariant variant = product.getVariants().stream()
                        .filter(v -> v.getId().equals(itemReq.getVariantId()))
                        .findFirst()
                        .orElse(null);

                if (variant == null) {
                    continue;
                }

                int currentQuantityInCart = cart.getItems().stream()
                        .filter(i -> i.getVariantId().equals(itemReq.getVariantId()))
                        .mapToInt(CartItem::getQuantity)
                        .sum();

                int finalTargetQuantity = currentQuantityInCart + itemReq.getQuantity();

                if (variant.getInventory().getStockQuantity() >= finalTargetQuantity) {
                    cart.addItem(itemReq.getProductId(), itemReq.getVariantId(), itemReq.getQuantity());
                }

            } catch (Exception e) {
                continue;
            }
        }

        repository.save(cart);
        return getCartData(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(Integer userId, Integer itemId, int quantity) {
        Cart cart = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng của người dùng"));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm này trong giỏ hàng"));

        Product product = productService.getProductEntity(item.getProductId());

        checkStockSufficiency(product, item.getVariantId(), quantity);

        cart.updateItemQuantity(itemId, quantity);
        repository.save(cart);
        return getCartData(cart);
    }

    @Transactional
    public CartResponse removeItemFromCart(Integer userId, Integer itemId) {
        Cart cart = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));
        cart.removeItem(itemId);
        repository.save(cart);
        return getCartData(cart);
    }

    @Transactional
    public void clearCart(Integer userId) {
        Cart cart = repository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cart.clearCart();
            repository.save(cart);
        }
    }

    private void checkStockSufficiency(Product product, Integer variantId, int finalTargetQuantity) {
        if (!product.isAvailableForPurchase()) {
            throw new IllegalStateException("Sản phẩm không hỗ trợ mua hàng tại thời điểm này");
        }

        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mẫu mã sản phẩm tương ứng"));

        if (variant.getInventory().getStockQuantity() < finalTargetQuantity) {
            throw new IllegalStateException("Số lượng sản phẩm trong kho không đủ để đáp ứng yêu cầu");
        }
    }

    private CartResponse getCartData(Cart cart) {
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