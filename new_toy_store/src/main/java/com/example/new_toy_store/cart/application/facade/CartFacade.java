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
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;
import com.example.new_toy_store.promotion.application.facade.PromotionFacade;
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
    private final PromotionFacade promotionFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final com.example.new_toy_store.order.application.OrderService orderService;

    public CartFacade(CartService cartService, ProductService productService,
                      PromotionFacade promotionFacade, ApplicationEventPublisher eventPublisher,
                      com.example.new_toy_store.order.application.OrderService orderService) {
        this.cartService = cartService;
        this.productService = productService;
        this.promotionFacade = promotionFacade;
        this.eventPublisher = eventPublisher;
        this.orderService = orderService;
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Integer userId, String promoCode) {
        Cart cart = cartService.getCartByUserId(userId);
        return buildCartResponse(cart, promoCode);
    }

    @Transactional
    public CartResponse addItem(Integer userId, AddCartItemRequest request) {
        Product product = productService.getProductEntity(request.getProductId());
        ProductVariant variant = findVariant(product, request.getProductId(), request.getVariantId());

        int availableQuantity = (variant.getInventory() != null) ? variant.getInventory().getStockQuantity() : 999;
        if (availableQuantity < request.getQuantity()) {
            throw CartCrossModuleException.insufficientStock(request.getProductId(), variant.getId(), request.getQuantity(), availableQuantity);
        }

        AddCartItemRequest validRequest = new AddCartItemRequest();
        validRequest.setProductId(request.getProductId());
        validRequest.setVariantId(variant.getId());
        validRequest.setQuantity(request.getQuantity());

        Cart cart = cartService.addItemToCart(userId, validRequest, variant.getPrice());
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
    public com.example.new_toy_store.order.application.dto.response.OrderResponse checkout(Integer userId, CheckoutCartRequest request) {
        Cart cart = cartService.lockCartForCheckout(userId);
        List<CartItem> selectedItems = cart.getItems().stream()
                .filter(CartItem::isSelected)
                .collect(Collectors.toList());

        if (selectedItems.isEmpty() && !cart.getItems().isEmpty()) {
            selectedItems = cart.getItems();
        }

        if (selectedItems.isEmpty()) {
            throw InvalidCartOperationException.emptyCart();
        }

        Set<Integer> productIds = selectedItems.stream().map(CartItem::getProductId).collect(Collectors.toSet());
        Map<Integer, Product> productMap = loadProductMap(productIds);

        com.example.new_toy_store.order.application.dto.request.OrderRequest orderRequest = new com.example.new_toy_store.order.application.dto.request.OrderRequest();
        orderRequest.setUserId(userId);
        orderRequest.setShippingAddress(request.getShippingAddress());
        orderRequest.setPromoCode(request.getPromoCode());

        List<com.example.new_toy_store.order.application.dto.request.OrderItemRequest> orderItems = selectedItems.stream()
                .map(item -> {
                    com.example.new_toy_store.order.application.dto.request.OrderItemRequest itemReq = new com.example.new_toy_store.order.application.dto.request.OrderItemRequest();
                    itemReq.setProductId(item.getProductId());
                    itemReq.setVariantId(item.getVariantId());
                    itemReq.setQuantity(item.getQuantity());
                    return itemReq;
                })
                .collect(Collectors.toList());

        orderRequest.setItems(orderItems);

        com.example.new_toy_store.order.application.dto.response.OrderResponse createdOrder = orderService.create(orderRequest, cart.getId());
        return createdOrder;
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

        int availableQuantity = (variant.getInventory() != null) ? variant.getInventory().getStockQuantity() : 999;
        if (availableQuantity < requestedQuantity) {
            throw CartCrossModuleException.insufficientStock(productId, variantId, requestedQuantity, availableQuantity);
        }

        return variant.getPrice();
    }

    private ProductVariant findVariant(Product product, Integer productId, Integer variantId) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            throw CartCrossModuleException.missingVariant(productId, variantId);
        }
        return product.getVariants().stream()
                .filter(variant -> variant.getId() != null && variant.getId().equals(variantId))
                .findFirst()
                .orElseGet(() -> product.getVariants().get(0));
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
            return CartMapper.toCartResponse(cart, Map.of(), List.of(), promoCode, promotionFacade);
        }

        Set<Integer> productIds = cart.getItems().stream()
                .map(CartItem::getProductId)
                .collect(Collectors.toSet());

        Map<Integer, Product> productMap = loadProductMap(productIds);
        List<PromotionResponse> activePromotions = promotionFacade.getActivePromotionsForProducts(productIds);

        return CartMapper.toCartResponse(cart, productMap, activePromotions, promoCode, promotionFacade);
    }
}
