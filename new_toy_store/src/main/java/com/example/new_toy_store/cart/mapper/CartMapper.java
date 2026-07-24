package com.example.new_toy_store.cart.mapper;

import com.example.new_toy_store.cart.application.dto.response.CartItemResponse;
import com.example.new_toy_store.cart.application.dto.response.CartResponse;
import com.example.new_toy_store.cart.domain.Cart;
import com.example.new_toy_store.cart.domain.CartItem;
import com.example.new_toy_store.cart.domain.CartStatus;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductImage;
import com.example.new_toy_store.product.domain.ProductVariant;
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;
import com.example.new_toy_store.promotion.application.facade.PromotionFacade;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class CartMapper {

    private CartMapper() {
    }

    public static CartResponse toCartResponse(Cart cart, Map<Integer, Product> productMap,
                                              List<PromotionResponse> activePromotions,
                                              String promoCode, PromotionFacade promotionFacade) {

        Map<Integer, List<PromotionResponse>> promotionsByProduct = groupPromotionsByProduct(activePromotions);
        List<CartItemResponse> itemResponses = toCartItemResponses(cart.getItems(), productMap, promotionsByProduct);
        CartSummary summary = buildCartSummary(itemResponses, promoCode, promotionFacade);
        CartNavigation navigation = buildCartNavigation(cart.getStatus(), itemResponses);

        return buildCartResponse(cart, itemResponses, summary, navigation);
    }

    private static CartResponse buildCartResponse(Cart cart,
                                                  List<CartItemResponse> itemResponses,
                                                  CartSummary summary,
                                                  CartNavigation navigation) {
        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                cart.getStatus(),
                navigation.allowedNextStates(),
                summary.cartTotal(),
                summary.appliedPromoCode(),
                summary.orderDiscountAmount(),
                summary.finalTotal(),
                summary.promoMessage(),
                itemResponses,
                navigation.allowedActions()
        );
    }

    private static List<CartItemResponse> toCartItemResponses(List<CartItem> items,
                                                              Map<Integer, Product> productMap,
                                                              Map<Integer, List<PromotionResponse>> promotionsByProduct) {
        return items.stream()
                .map(item -> toCartItemResponse(
                        item,
                        productMap.get(item.getProductId()),
                        promotionsByProduct.getOrDefault(item.getProductId(), List.of())
                ))
                .collect(Collectors.toList());
    }

    private static CartItemResponse toCartItemResponse(CartItem item,
                                                       Product product,
                                                       List<PromotionResponse> activePromotions) {
        ProductVariant variant = findVariant(product, item.getVariantId());
        ProductSnapshot productSnapshot = buildProductSnapshot(product, variant);
        PriceSnapshot priceSnapshot = buildPriceSnapshot(product, variant, activePromotions);
        ItemAvailability availability = evaluateItemAvailability(product, variant, item.getQuantity());
        PriceChange priceChange = evaluatePriceChange(variant, item.getAddedPrice());

        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getVariantId(),
                productSnapshot.name(),
                productSnapshot.variantAttributes(),
                productSnapshot.thumbnailUrl(),
                item.getAddedPrice(),
                priceSnapshot.originalPrice(),
                priceSnapshot.finalPrice(),
                item.getQuantity(),
                item.isSelected(),
                availability.isAvailable(),
                priceChange.hasChanged(),
                resolveItemMessage(availability, priceChange)
        );
    }

    private static ProductSnapshot buildProductSnapshot(Product product, ProductVariant variant) {
        String name = product != null ? product.getName() : "Sản phẩm không hợp lệ";
        String attributes = variant != null ? variant.generateAttributesSnapshot() : "";
        String thumbnail = findThumbnailUrl(product);

        return new ProductSnapshot(name, attributes, thumbnail);
    }

    private static PriceSnapshot buildPriceSnapshot(Product product,
                                                    ProductVariant variant,
                                                    List<PromotionResponse> activePromotions) {
        double originalPrice = variant != null ? variant.getPrice() : 0.0;
        double itemDiscount = calculateBestItemDiscount(product, originalPrice, activePromotions);
        double finalPrice = roundMoney(Math.max(0.0, originalPrice - itemDiscount));

        return new PriceSnapshot(originalPrice, finalPrice);
    }

    private static String resolveItemMessage(ItemAvailability availability, PriceChange priceChange) {
        return priceChange.hasChanged() ? priceChange.message() : availability.message();
    }

    private static CartSummary buildCartSummary(List<CartItemResponse> itemResponses,
                                                String promoCode,
                                                PromotionFacade promotionFacade) {
        double cartTotal = calculateSelectedItemsTotal(itemResponses);
        OrderPromotionResult promotionResult = applyOrderPromotion(promoCode, cartTotal, promotionFacade);
        double finalTotal = roundMoney(Math.max(0.0, cartTotal - promotionResult.discountAmount()));

        return new CartSummary(
                cartTotal,
                promotionResult.appliedCode(),
                promotionResult.discountAmount(),
                finalTotal,
                promotionResult.message()
        );
    }

    private static CartNavigation buildCartNavigation(CartStatus status, List<CartItemResponse> itemResponses) {
        List<CartStatus> allowedNextStates = status != null ? status.getNextValidStates() : List.of();
        List<String> allowedActions = determineAllowedUiActions(status, itemResponses);

        return new CartNavigation(allowedNextStates, allowedActions);
    }

    static List<String> determineAllowedUiActions(CartStatus status, List<CartItemResponse> itemResponses) {
        if (status != CartStatus.ACTIVE) {
            return List.of();
        }
        if (itemResponses.isEmpty()) {
            return List.of("CONTINUE_SHOPPING");
        }

        boolean hasSelectedAndAvailable = itemResponses.stream()
                .anyMatch(item -> item.isSelected() && item.isAvailable());
        boolean hasBlockingWarnings = itemResponses.stream()
                .anyMatch(item -> item.isSelected() && (!item.isAvailable() || item.hasPriceChanged()));

        if (hasBlockingWarnings) {
            return List.of("CONTINUE_SHOPPING", "CLEAR_CART", "REVIEW_CART_WARNINGS");
        }
        if (hasSelectedAndAvailable) {
            return List.of("CONTINUE_SHOPPING", "CLEAR_CART", "PROCEED_TO_CHECKOUT");
        }
        return List.of("CONTINUE_SHOPPING", "CLEAR_CART");
    }

    private static double calculateSelectedItemsTotal(List<CartItemResponse> itemResponses) {
        double sum = itemResponses.stream()
                .filter(CartMapper::isSelectedAvailableItem)
                .mapToDouble(CartMapper::calculateItemLineTotal)
                .sum();
        return roundMoney(sum);
    }

    private static boolean isSelectedAvailableItem(CartItemResponse item) {
        return item.isAvailable() && item.isSelected();
    }

    private static double calculateItemLineTotal(CartItemResponse item) {
        return item.getFinalPrice() * item.getQuantity();
    }

    private static Map<Integer, List<PromotionResponse>> groupPromotionsByProduct(
            List<PromotionResponse> activePromotions
    ) {
        if (activePromotions == null || activePromotions.isEmpty()) {
            return Map.of();
        }

        return activePromotions.stream()
                .filter(promotion -> promotion.getTargetProductId() != null)
                .collect(Collectors.groupingBy(PromotionResponse::getTargetProductId));
    }

    private static double calculateBestItemDiscount(Product product,
                                                    double originalPrice,
                                                    List<PromotionResponse> activePromotions) {
        if (product == null || activePromotions == null) {
            return 0.0;
        }

        return activePromotions.stream()
                .filter(promotion -> product.getId().equals(promotion.getTargetProductId()))
                .map(promotion -> calculatePromotionDiscount(promotion, originalPrice))
                .max(Double::compareTo)
                .orElse(0.0);
    }

    private static double calculatePromotionDiscount(PromotionResponse promotion, double originalPrice) {
        double discount = isPercentagePromotion(promotion)
                ? originalPrice * (promotion.getDiscountValue() / 100.0)
                : promotion.getDiscountValue();
        return promotion.getMaxDiscountAmount() != null ? Math.min(discount, promotion.getMaxDiscountAmount()) : discount;
    }

    private static boolean isPercentagePromotion(PromotionResponse promotion) {
        return promotion.getType() != null && "PERCENTAGE".equalsIgnoreCase(promotion.getType().getCode());
    }

    private static OrderPromotionResult applyOrderPromotion(String promoCode,
                                                           double cartTotal,
                                                           PromotionFacade promotionFacade) {
        if (promoCode == null || promoCode.trim().isEmpty() || promotionFacade == null) {
            return new OrderPromotionResult(null, 0.0, null);
        }

        try {
            double discountAmount = promotionFacade.calculateOrderDiscount(promoCode, cartTotal);
            if (discountAmount > 0) {
                return new OrderPromotionResult(promoCode.toUpperCase().trim(), discountAmount, "Áp dụng mã giảm giá thành công");
            }
            return new OrderPromotionResult(null, 0.0, "Mã giảm giá không mang lại ưu đãi cho đơn hàng này");
        } catch (RuntimeException ex) {
            return new OrderPromotionResult(null, 0.0, ex.getMessage());
        }
    }

    private static ItemAvailability evaluateItemAvailability(Product product, ProductVariant variant, int requestedQuantity) {
        if (product == null) {
            return new ItemAvailability(false, "Sản phẩm không còn tồn tại trên hệ thống");
        }
        if (variant == null) {
            return new ItemAvailability(false, "Mẫu mã này đã ngừng phân phối");
        }
        if (!product.isAvailableForPurchase()) {
            return new ItemAvailability(false, "Sản phẩm đang tạm ngừng kinh doanh");
        }

        int stock = variant.getInventory() != null ? variant.getInventory().getStockQuantity() : 0;
        if (stock < requestedQuantity) {
            return new ItemAvailability(false, "Rất tiếc, kho chỉ còn lại " + stock + " sản phẩm");
        }

        return new ItemAvailability(true, "Sẵn sàng thanh toán");
    }

    private static PriceChange evaluatePriceChange(ProductVariant variant, double addedPrice) {
        if (variant == null) {
            return new PriceChange(false, null);
        }

        double currentPrice = variant.getPrice();
        if (addedPrice < currentPrice) {
            return new PriceChange(true, "Sản phẩm đã tăng giá so với lúc bạn thêm vào giỏ hàng.");
        }
        if (addedPrice > currentPrice) {
            return new PriceChange(true, "Sản phẩm đang giảm giá! Hãy thanh toán ngay.");
        }

        return new PriceChange(false, null);
    }

    private static ProductVariant findVariant(Product product, Integer variantId) {
        if (product == null || product.getVariants() == null) {
            return null;
        }

        return product.getVariants()
                .stream()
                .filter(variant -> variant.getId().equals(variantId))
                .findFirst()
                .orElse(null);
    }

    private static String findThumbnailUrl(Product product) {
        if (product == null || product.getImages() == null) {
            return null;
        }

        return product.getImages()
                .stream()
                .filter(ProductImage::isThumbnail)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse(null);
    }

    private static double roundMoney(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }

    private record ProductSnapshot(String name, String variantAttributes, String thumbnailUrl) {}
    private record PriceSnapshot(double originalPrice, double finalPrice) {}
    private record ItemAvailability(boolean isAvailable, String message) {}
    private record PriceChange(boolean hasChanged, String message) {}
    private record CartSummary(double cartTotal, String appliedPromoCode, double orderDiscountAmount,
                               double finalTotal, String promoMessage) {}
    private record CartNavigation(List<CartStatus> allowedNextStates, List<String> allowedActions) {}
    private record OrderPromotionResult(String appliedCode, double discountAmount, String message) {}
}
