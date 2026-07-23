package com.example.new_toy_store.cart.application.facade;

import com.example.new_toy_store.cart.application.dto.request.AddCartItemRequest;
import com.example.new_toy_store.cart.application.dto.request.CheckoutCartRequest;
import com.example.new_toy_store.cart.application.dto.request.SyncCartItemRequest;
import com.example.new_toy_store.cart.application.dto.request.SyncCartRequest;
import com.example.new_toy_store.cart.application.dto.request.ToggleCartItemSelectionRequest;
import com.example.new_toy_store.cart.application.dto.request.UpdateCartItemQuantityRequest;
import com.example.new_toy_store.cart.application.dto.response.CartResponse;
import com.example.new_toy_store.cart.application.service.CartService;
import com.example.new_toy_store.cart.domain.Cart;
import com.example.new_toy_store.cart.domain.CartItem;
import com.example.new_toy_store.cart.domain.exception.CartCrossModuleException;
import com.example.new_toy_store.cart.domain.exception.CartDataConflictException;
import com.example.new_toy_store.cart.domain.exception.InvalidCartOperationException;
import com.example.new_toy_store.cart.mapper.CartMapper;
import com.example.new_toy_store.global.event.CartCheckoutItemPayload;
import com.example.new_toy_store.global.event.CartCheckoutRequestedEvent;
import com.example.new_toy_store.product.application.service.ProductService;
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

    public CartFacade(CartService cartService, ProductService productService,
                      PromotionService promotionService, ApplicationEventPublisher eventPublisher) {
        this.cartService = cartService;
        this.productService = productService;
        this.promotionService = promotionService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Integer userId, String promoCode) {
        Cart cart = cartService.getCartByUserId(userId);
        return buildCartResponse(cart, promoCode);
    }

    @Transactional
    public CartResponse addItem(Integer userId, AddCartItemRequest request) {
        double currentPrice = getPriceAndCheckStock(request.getProductId(), request.getVariantId(), request.getQuantity());
        Cart cart = cartService.addItemToCart(userId, request, currentPrice);
        return buildCartResponse(cart, null);
    }

    @Transactional
    public CartResponse syncCart(Integer userId, SyncCartRequest request) {
        Map<Integer, Double> variantPrices = new HashMap<>();
        Set<Integer> productIds = request.getItems().stream()
                .map(SyncCartItemRequest::getProductId)
                .collect(Collectors.toSet());
        Map<Integer, Product> productMap = loadProductMap(productIds);

        for (SyncCartItemRequest itemReq : request.getItems()) {
            Product product = productMap.get(itemReq.getProductId());
            double price = getPriceAndCheckStock(
                    product,
                    itemReq.getProductId(),
                    itemReq.getVariantId(),
                    itemReq.getQuantity()
            );
            variantPrices.put(itemReq.getVariantId(), price);
        }

        Cart cart = cartService.syncCart(userId, request, variantPrices);
        return buildCartResponse(cart, null);
    }

    @Transactional
    public CartResponse updateQuantity(Integer userId, Integer itemId, UpdateCartItemQuantityRequest request) {
        Cart cart = cartService.updateItemQuantity(userId, itemId, request.getQuantity());
        return buildCartResponse(cart, null);
    }

    @Transactional
    public CartResponse toggleSelection(Integer userId, Integer itemId, ToggleCartItemSelectionRequest request) {
        Cart cart = cartService.toggleItemSelection(userId, itemId, request.isSelected());
        return buildCartResponse(cart, null);
    }

    @Transactional
    public CartResponse removeItem(Integer userId, Integer itemId) {
        Cart cart = cartService.removeItemFromCart(userId, itemId);
        return buildCartResponse(cart, null);
    }

    public void clearCart(Integer userId) {
        cartService.clearCart(userId);
    }

    @Transactional
    public void checkout(Integer userId, CheckoutCartRequest request) {
        Cart cart = cartService.lockCartForCheckout(userId);
        List<CartItem> selectedItems = cart.getItems().stream()
                .filter(CartItem::isSelected)
                .collect(Collectors.toList());

        if (selectedItems.isEmpty()) {
            throw InvalidCartOperationException.emptyCart();
        }

        Set<Integer> productIds = selectedItems.stream().map(CartItem::getProductId).collect(Collectors.toSet());
        Map<Integer, Product> productMap = loadProductMap(productIds);

        List<CartCheckoutItemPayload> eventItems = selectedItems.stream()
                .map(item -> toCheckoutItemPayload(item, productMap))
                .collect(Collectors.toList());

        eventPublisher.publishEvent(new CartCheckoutRequestedEvent(
                cart.getId(),
                cart.getUserId(),
                request.getShippingAddress(),
                request.getPromoCode(),
                eventItems
        ));
    }

    private CartCheckoutItemPayload toCheckoutItemPayload(CartItem item, Map<Integer, Product> productMap) {
        Product product = productMap.get(item.getProductId());
        if (product == null) {
            throw CartCrossModuleException.missingProduct(item.getProductId());
        }

        ProductVariant variant = findVariant(product, item.getProductId(), item.getVariantId());
        if (item.getAddedPrice() != variant.getPrice()) {
            throw CartDataConflictException.priceChanged(
                    product.getId(),
                    variant.getId(),
                    item.getAddedPrice(),
                    variant.getPrice()
            );
        }

        return new CartCheckoutItemPayload(
                item.getProductId(),
                item.getVariantId(),
                product.getName(),
                variant.generateAttributesSnapshot(),
                item.getQuantity(),
                item.getAddedPrice()
        );
    }

    private double getPriceAndCheckStock(Integer productId, Integer variantId, int requestedQuantity) {
        Product product = productService.getProductEntity(productId);
        return getPriceAndCheckStock(product, productId, variantId, requestedQuantity);
    }

    private double getPriceAndCheckStock(
            Product product,
            Integer productId,
            Integer variantId,
            int requestedQuantity
    ) {
        if (product == null) {
            throw CartCrossModuleException.missingProduct(productId);
        }

        if (!product.isAvailableForPurchase()) {
            throw CartDataConflictException.softDeletedProduct(productId);
        }

        ProductVariant variant = findVariant(product, productId, variantId);
        if (variant.getInventory() == null) {
            throw CartCrossModuleException.invalidInventory(productId, variantId);
        }

        int availableQuantity = variant.getInventory().getStockQuantity();
        if (availableQuantity < requestedQuantity) {
            throw CartCrossModuleException.insufficientStock(productId, variantId, requestedQuantity, availableQuantity);
        }

        return variant.getPrice();
    }

    private ProductVariant findVariant(Product product, Integer productId, Integer variantId) {
        return product.getVariants().stream()
                .filter(variant -> variant.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> CartCrossModuleException.missingVariant(productId, variantId));
    }

    private Map<Integer, Product> loadProductMap(Set<Integer> productIds) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        return productService.getProductsByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
    }

    private CartResponse buildCartResponse(Cart cart, String promoCode) {
        if (cart.getItems().isEmpty()) {
            return CartMapper.toCartResponse(cart, Map.of(), List.of(), promoCode, promotionService);
        }

        Set<Integer> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());

        Map<Integer, Product> productMap = loadProductMap(productIds);
        List<PromotionResponse> activePromotions = promotionService.getActivePromotionsForProducts(productIds);

        return CartMapper.toCartResponse(cart, productMap, activePromotions, promoCode, promotionService);
    }
}
