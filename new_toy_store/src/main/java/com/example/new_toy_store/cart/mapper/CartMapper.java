package com.example.new_toy_store.cart.mapper;

import com.example.new_toy_store.cart.application.dto.response.CartItemResponse;
import com.example.new_toy_store.cart.application.dto.response.CartResponse;
import com.example.new_toy_store.cart.domain.Cart;
import com.example.new_toy_store.cart.domain.CartItem;
import com.example.new_toy_store.cart.domain.CartStatus;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductImage;
import com.example.new_toy_store.product.domain.ProductVariant;
import com.example.new_toy_store.promotion.application.PromotionService;
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CartMapper {

    public static CartResponse toResponse(Cart cart, Map<Integer, Product> productMap,
                                          List<PromotionResponse> activePromotions,
                                          String promoCode, PromotionService promotionService) {

        Map<Integer, List<PromotionResponse>> promotionsByProduct = indexPromotionsByProduct(activePromotions);
        List<CartItemResponse> itemResponses = mapCartItems(cart.getItems(), productMap, promotionsByProduct);

        double cartTotal = calculateCartTotal(itemResponses);

        OrderPromotionResult promotionResult = applyOrderPromotion(promoCode, cartTotal, promotionService);
        double finalTotal = calculateFinalTotal(cartTotal, promotionResult.discountAmount);

        CartStatus currentStatus = cart.getStatus();
        List<String> allowedActions = generateAllowedActions(currentStatus, itemResponses);
        List<CartStatus> nextStates = currentStatus != null ? currentStatus.getNextValidStates() : null;

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                currentStatus,
                nextStates,
                cartTotal,
                promotionResult.appliedCode,
                promotionResult.discountAmount,
                finalTotal,
                promotionResult.message,
                itemResponses,
                allowedActions
        );
    }

    private static List<CartItemResponse> mapCartItems(List<CartItem> items,
                                                       Map<Integer, Product> productMap,
                                                       Map<Integer, List<PromotionResponse>> promotionsByProduct) {
        return items.stream()
                .map(item -> buildItemResponse(
                        item,
                        productMap.get(item.getProductId()),
                        promotionsByProduct.getOrDefault(item.getProductId(), List.of())
                ))
                .collect(Collectors.toList());
    }

    private static Map<Integer, List<PromotionResponse>> indexPromotionsByProduct(
            List<PromotionResponse> activePromotions
    ) {
        if (activePromotions == null || activePromotions.isEmpty()) {
            return Map.of();
        }

        return activePromotions.stream()
                .filter(promotion -> promotion.getTargetProductId() != null)
                .collect(Collectors.groupingBy(PromotionResponse::getTargetProductId));
    }

    private static CartItemResponse buildItemResponse(CartItem item, Product product, List<PromotionResponse> activePromotions) {
        ProductVariant variant = extractVariant(product, item.getVariantId());

        ItemStatus status = evaluateItemStatus(product, variant, item.getQuantity());
        PriceStatus priceStatus = evaluatePriceChange(variant, item.getAddedPrice());

        String name = product != null ? product.getName() : "Sản phẩm không hợp lệ";
        String attributes = variant != null ? variant.generateAttributesSnapshot() : "";
        String thumbnail = extractThumbnail(product);

        double originalPrice = variant != null ? variant.getPrice() : 0.0;
        double itemDiscount = calculateItemDiscount(product, originalPrice, activePromotions);
        double finalPrice = Math.max(0.0, Math.round((originalPrice - itemDiscount) * 100.0) / 100.0);

        return new CartItemResponse(
                item.getId(),
                item.getProductId(),
                item.getVariantId(),
                name,
                attributes,
                thumbnail,
                item.getAddedPrice(),
                originalPrice,
                finalPrice,
                item.getQuantity(),
                item.isSelected(),
                status.isAvailable,
                priceStatus.hasChanged,
                priceStatus.hasChanged ? priceStatus.message : status.message
        );
    }

    private static double calculateCartTotal(List<CartItemResponse> itemResponses) {
        double sum = itemResponses.stream()
                .filter(i -> i.isAvailable() && i.isSelected())
                .mapToDouble(i -> i.getFinalPrice() * i.getQuantity())
                .sum();
        return Math.round(sum * 100.0) / 100.0;
    }

    private static double calculateFinalTotal(double cartTotal, double discountAmount) {
        return Math.max(0.0, Math.round((cartTotal - discountAmount) * 100.0) / 100.0);
    }

    private static double calculateItemDiscount(Product product, double originalPrice, List<PromotionResponse> activePromotions) {
        if (product == null || activePromotions == null) return 0.0;

        return activePromotions.stream()
                .filter(p -> p.getTargetProductId() != null && p.getTargetProductId().equals(product.getId()))
                .map(p -> {
                    double discount = ("PERCENTAGE".equalsIgnoreCase(p.getType()) || "PERCENT".equalsIgnoreCase(p.getType()))
                            ? originalPrice * (p.getDiscountValue() / 100.0)
                            : p.getDiscountValue();
                    return p.getMaxDiscountAmount() != null ? Math.min(discount, p.getMaxDiscountAmount()) : discount;
                })
                .max(Double::compareTo)
                .orElse(0.0);
    }

    private static OrderPromotionResult applyOrderPromotion(String promoCode, double cartTotal, PromotionService promotionService) {
        if (promoCode == null || promoCode.trim().isEmpty() || promotionService == null) {
            return new OrderPromotionResult(null, 0.0, null);
        }

        try {
            double discountAmount = promotionService.calculateOrderDiscount(promoCode, cartTotal);
            if (discountAmount > 0) {
                return new OrderPromotionResult(promoCode.toUpperCase().trim(), discountAmount, "Áp dụng mã giảm giá thành công");
            }
            return new OrderPromotionResult(null, 0.0, "Mã giảm giá không mang lại ưu đãi cho đơn hàng này");
        } catch (RuntimeException ex) {
            return new OrderPromotionResult(null, 0.0, ex.getMessage());
        }
    }

    static List<String> generateAllowedActions(CartStatus status, List<CartItemResponse> itemResponses) {
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

    private static ItemStatus evaluateItemStatus(Product product, ProductVariant variant, int requestedQuantity) {
        if (product == null) return new ItemStatus(false, "Sản phẩm không còn tồn tại trên hệ thống");
        if (variant == null) return new ItemStatus(false, "Mẫu mã này đã ngừng phân phối");
        if (!product.isAvailableForPurchase()) return new ItemStatus(false, "Sản phẩm đang tạm ngừng kinh doanh");

        int stock = variant.getInventory() != null ? variant.getInventory().getStockQuantity() : 0;
        if (stock < requestedQuantity) return new ItemStatus(false, "Rất tiếc, kho chỉ còn lại " + stock + " sản phẩm");

        return new ItemStatus(true, "Sẵn sàng thanh toán");
    }

    private static PriceStatus evaluatePriceChange(ProductVariant variant, double addedPrice) {
        if (variant == null) return new PriceStatus(false, null);
        double currentPrice = variant.getPrice();

        if (addedPrice < currentPrice) return new PriceStatus(true, "Sản phẩm đã tăng giá so với lúc bạn thêm vào giỏ hàng.");
        if (addedPrice > currentPrice) return new PriceStatus(true, "Sản phẩm đang giảm giá! Hãy thanh toán ngay.");

        return new PriceStatus(false, null);
    }

    private static ProductVariant extractVariant(Product product, Integer variantId) {
        if (product == null || product.getVariants() == null) return null;
        return product.getVariants().stream().filter(v -> v.getId().equals(variantId)).findFirst().orElse(null);
    }

    private static String extractThumbnail(Product product) {
        if (product == null || product.getImages() == null) return null;
        return product.getImages().stream().filter(ProductImage::isThumbnail).map(ProductImage::getImageUrl).findFirst().orElse(null);
    }

    private record ItemStatus(boolean isAvailable, String message) {}
    private record PriceStatus(boolean hasChanged, String message) {}
    private record OrderPromotionResult(String appliedCode, double discountAmount, String message) {}
}
