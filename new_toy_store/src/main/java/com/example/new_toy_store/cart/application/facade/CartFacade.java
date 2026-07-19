package com.example.new_toy_store.cart.application.facade;

import com.example.new_toy_store.cart.application.service.CartService;
import com.example.new_toy_store.cart.application.dto.request.CartItemRequest;
import com.example.new_toy_store.cart.application.dto.request.CartRequest;
import com.example.new_toy_store.cart.application.dto.request.CheckoutRequest;
import com.example.new_toy_store.cart.application.dto.response.CartResponse;
import com.example.new_toy_store.cart.domain.Cart;
import com.example.new_toy_store.cart.domain.CartItem;
import com.example.new_toy_store.cart.mapper.CartMapper;
import com.example.new_toy_store.global.event.CartCheckoutRequestedEvent;
import com.example.new_toy_store.product.application.ProductService;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductVariant;
import com.example.new_toy_store.promotion.application.PromotionService;
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CartFacade {

    private final CartService cartService;
    private final ProductService productService;
    private final PromotionService promotionService;
    private final ApplicationEventPublisher eventPublisher;

    public CartFacade(CartService cartService, ProductService productService, PromotionService promotionService, ApplicationEventPublisher eventPublisher) {
        this.cartService = cartService;
        this.productService = productService;
        this.promotionService = promotionService;
        this.eventPublisher = eventPublisher;
    }

    public CartResponse getCart(Integer userId, String promoCode) {
        Cart cart = cartService.getCartByUserId(userId);
        return buildCartResponse(cart, promoCode);
    }

    public CartResponse addItem(Integer userId, CartItemRequest request) {
        double currentPrice = getPriceAndCheckStock(request.getProductId(), request.getVariantId(), request.getQuantity());
        Cart cart = cartService.addItemToCart(userId, request, currentPrice);
        return buildCartResponse(cart, null);
    }

    public CartResponse syncCart(Integer userId, CartRequest request) {
        Map<Integer, Double> variantPrices = new HashMap<>();

        for (CartItemRequest itemReq : request.getItems()) {
            double price = getPriceAndCheckStock(itemReq.getProductId(), itemReq.getVariantId(), itemReq.getQuantity());
            variantPrices.put(itemReq.getVariantId(), price);
        }

        Cart cart = cartService.syncCart(userId, request, variantPrices);
        return buildCartResponse(cart, null);
    }

    public CartResponse updateQuantity(Integer userId, Integer itemId, int quantity) {
        Cart cart = cartService.updateItemQuantity(userId, itemId, quantity);
        return buildCartResponse(cart, null);
    }

    public CartResponse toggleSelection(Integer userId, Integer itemId, boolean isSelected) {
        Cart cart = cartService.toggleItemSelection(userId, itemId, isSelected);
        return buildCartResponse(cart, null);
    }

    public CartResponse removeItem(Integer userId, Integer itemId) {
        Cart cart = cartService.removeItemFromCart(userId, itemId);
        return buildCartResponse(cart, null);
    }

    public void clearCart(Integer userId) {
        cartService.clearCart(userId);
    }

    @Transactional
    public void checkout(Integer userId, CheckoutRequest request) {
        Cart cart = cartService.lockCartForCheckout(userId);
        List<CartItem> selectedItems = cart.getItems().stream()
                .filter(CartItem::isSelected)
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống hoặc không có sản phẩm nào được chọn để thanh toán");
        }

        Set<Integer> productIds = selectedItems.stream().map(CartItem::getProductId).collect(Collectors.toSet());
        Map<Integer, Product> productMap = productService.getProductsByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<CartCheckoutRequestedEvent.CheckoutItemDetail> eventItems = selectedItems.stream().map(item -> {
            Product product = productMap.get(item.getProductId());
            ProductVariant variant = product.getVariants().stream()
                    .filter(v -> v.getId().equals(item.getVariantId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Lỗi dữ liệu: Không tìm thấy biến thể"));

            return new CartCheckoutRequestedEvent.CheckoutItemDetail(
                    item.getProductId(),
                    item.getVariantId(),
                    product.getName(),
                    variant.generateAttributesSnapshot(),
                    item.getQuantity(),
                    item.getAddedPrice()
            );
        }).collect(Collectors.toList());

        eventPublisher.publishEvent(new CartCheckoutRequestedEvent(
                cart.getId(),
                cart.getUserId(),
                request.getShippingAddress(),
                request.getPromoCode(),
                eventItems
        ));
    }

    private double getPriceAndCheckStock(Integer productId, Integer variantId, int requestedQuantity) {
        Product product = productService.getProductEntity(productId);

        if (!product.isAvailableForPurchase()) {
            throw new IllegalStateException("Sản phẩm không hỗ trợ mua hàng tại thời điểm này");
        }

        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mẫu mã sản phẩm tương ứng"));

        if (variant.getInventory().getStockQuantity() < requestedQuantity) {
            throw new IllegalStateException("Số lượng sản phẩm trong kho không đủ để đáp ứng yêu cầu");
        }

        return variant.getPrice();
    }

    private CartResponse buildCartResponse(Cart cart, String promoCode) {
        if (cart.getItems().isEmpty()) {
            return CartMapper.toResponse(cart, Map.of(), List.of(), promoCode, promotionService);
        }

        Set<Integer> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());

        Map<Integer, Product> productMap = productService.getProductsByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        List<PromotionResponse> activePromotions = promotionService.getActivePromotionsForProducts(productIds);

        return CartMapper.toResponse(cart, productMap, activePromotions, promoCode, promotionService);
    }
}