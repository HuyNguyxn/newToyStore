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
import com.example.new_toy_store.promotion.application.PromotionService;
import com.example.new_toy_store.promotion.domain.Promotion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository repository;
    private final ProductService productService;
    private final PromotionService promotionService;

    public CartService(CartRepository repository, ProductService productService, PromotionService promotionService) {
        this.repository = repository;
        this.productService = productService;
        this.promotionService = promotionService;
    }

    @Transactional(readOnly = true)
    public CartResponse getCartByUserId(Integer userId) {
        Cart cart = repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giỏ hàng của người dùng yêu cầu"));
        return getCartData(cart);
    }

    @Transactional
    public CartResponse addItemToCart(Integer userId, CartItemRequest request) {
        Cart cart = repository.findByUserId(userId)
                .orElseGet(() -> repository.save(new Cart(userId)));

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
        Cart cart = repository.findByUserId(userId)
                .orElseGet(() -> repository.save(new Cart(userId)));

        cart.clearCart();

        for (CartItemRequest itemReq : request.getItems()) {
            Product product = productService.getProductEntity(itemReq.getProductId());
            checkStockSufficiency(product, itemReq.getVariantId(), itemReq.getQuantity());
            cart.addItem(itemReq.getProductId(), itemReq.getVariantId(), itemReq.getQuantity());
        }

        repository.save(cart);
        return getCartData(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(Integer userId, Integer itemId, int quantity) {
        Cart cart = repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giỏ hàng tương ứng"));

        CartItem targetItem = cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không có trong giỏ hàng"));

        Product product = productService.getProductEntity(targetItem.getProductId());
        checkStockSufficiency(product, targetItem.getVariantId(), quantity);

        cart.updateItemQuantity(itemId, quantity);
        repository.save(cart);
        return getCartData(cart);
    }

    @Transactional
    public CartResponse removeItemFromCart(Integer userId, Integer itemId) {
        Cart cart = repository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giỏ hàng tương ứng"));

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
            return CartMapper.toResponse(cart, Map.of(), List.of());
        }

        Set<Integer> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());

        Map<Integer, Product> productMap = productService.getProductsByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<Promotion> activePromotions = promotionService.getActivePromotionsForProducts(productIds);

        return CartMapper.toResponse(cart, productMap, activePromotions);
    }
}