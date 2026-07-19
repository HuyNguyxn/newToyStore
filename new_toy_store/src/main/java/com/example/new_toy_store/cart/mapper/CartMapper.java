package com.example.new_toy_store.cart.mapper;

import com.example.new_toy_store.cart.application.dto.response.CartItemResponse;
import com.example.new_toy_store.cart.application.dto.response.CartResponse;
import com.example.new_toy_store.cart.domain.Cart;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductImage;
import com.example.new_toy_store.product.domain.ProductVariant;
import com.example.new_toy_store.promotion.application.PromotionService;
import com.example.new_toy_store.promotion.application.dto.response.PromotionResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CartMapper {

    // SỬA LỖI TẠI ĐÂY: Tham số thứ 3 đổi thành List<PromotionResponse>
    public static CartResponse toResponse(Cart cart, Map<Integer, Product> productMap,
                                          List<PromotionResponse> activePromotions,
                                          String promoCode, PromotionService promotionService) {

        List<CartItemResponse> itemResponses = cart.getItems().stream().map(item -> {
            Product product = productMap.get(item.getProductId());
            ProductVariant variant = product != null ? product.getVariants().stream()
                    .filter(v -> v.getId().equals(item.getVariantId()))
                    .findFirst().orElse(null) : null;

            boolean isAvailable = true;
            String message = "Sẵn sàng thanh toán";
            boolean hasPriceChanged = false;

            if (product == null) {
                isAvailable = false;
                message = "Sản phẩm không còn tồn tại trên hệ thống";
            } else if (variant == null) {
                isAvailable = false;
                message = "Mẫu mã này đã ngừng phân phối";
            } else if (!product.isAvailableForPurchase()) {
                isAvailable = false;
                message = "Sản phẩm đang tạm ngừng kinh doanh";
            } else if (variant.getInventory() == null || variant.getInventory().getStockQuantity() < item.getQuantity()) {
                isAvailable = false;
                message = "Rất tiếc, kho chỉ còn lại " +
                        (variant.getInventory() != null ? variant.getInventory().getStockQuantity() : 0) + " sản phẩm";
            }

            String name = product != null ? product.getName() : "Sản phẩm không hợp lệ";
            String attributes = variant != null ? variant.generateAttributesSnapshot() : "";
            double originalPrice = variant != null ? variant.getPrice() : 0.0;

            if (variant != null && item.getAddedPrice() != originalPrice) {
                hasPriceChanged = true;
                message = item.getAddedPrice() < originalPrice ?
                        "Sản phẩm đã tăng giá so với lúc bạn thêm vào giỏ hàng." :
                        "Sản phẩm đang giảm giá! Hãy thanh toán ngay.";
            }

            double discountAmount = 0.0;
            if (product != null && activePromotions != null) {
                discountAmount = activePromotions.stream()
                        .filter(p -> p.getTargetProductId() != null && p.getTargetProductId().equals(product.getId()))
                        .map(p -> {
                            double discount = 0.0;
                            if ("PERCENTAGE".equalsIgnoreCase(p.getType()) || "PERCENT".equalsIgnoreCase(p.getType())) {
                                discount = originalPrice * (p.getDiscountValue() / 100.0);
                                if (p.getMaxDiscountAmount() != null && discount > p.getMaxDiscountAmount()) {
                                    discount = p.getMaxDiscountAmount();
                                }
                            } else {
                                discount = p.getDiscountValue();
                            }
                            return discount;
                        })
                        .max(Double::compareTo)
                        .orElse(0.0);
            }

            double finalPrice = Math.max(0.0, Math.round((originalPrice - discountAmount) * 100.0) / 100.0);

            String thumbnail = product != null ? product.getImages().stream()
                    .filter(ProductImage::isThumbnail).map(ProductImage::getImageUrl)
                    .findFirst().orElse(null) : null;

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
                    isAvailable,
                    hasPriceChanged,
                    message
            );
        }).collect(Collectors.toList());

        double cartTotal = itemResponses.stream()
                .filter(i -> i.isAvailable() && i.isSelected())
                .mapToDouble(i -> i.getFinalPrice() * i.getQuantity())
                .sum();

        cartTotal = Math.round(cartTotal * 100.0) / 100.0;

        double orderDiscountAmount = 0.0;
        String appliedPromoCode = null;
        String promoMessage = null;

        if (promoCode != null && !promoCode.trim().isEmpty() && promotionService != null) {
            try {
                orderDiscountAmount = promotionService.calculateOrderDiscount(promoCode, cartTotal);
                if (orderDiscountAmount > 0) {
                    appliedPromoCode = promoCode.toUpperCase().trim();
                    promoMessage = "Áp dụng mã giảm giá thành công";
                } else {
                    promoMessage = "Mã giảm giá không mang lại ưu đãi cho đơn hàng này";
                }
            } catch (RuntimeException ex) {
                promoMessage = ex.getMessage();
            }
        }

        double finalTotal = Math.max(0.0, Math.round((cartTotal - orderDiscountAmount) * 100.0) / 100.0);

        return new CartResponse(
                cart.getId(),
                cart.getUserId(),
                cartTotal,
                appliedPromoCode,
                orderDiscountAmount,
                finalTotal,
                promoMessage,
                itemResponses
        );
    }
}