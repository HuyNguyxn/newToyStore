package com.example.new_toy_store.cart.mapper;

import com.example.new_toy_store.cart.application.dto.response.CartItemResponse;
import com.example.new_toy_store.cart.application.dto.response.CartResponse;
import com.example.new_toy_store.cart.domain.Cart;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductImage;
import com.example.new_toy_store.product.domain.ProductVariant;
import com.example.new_toy_store.promotion.domain.Promotion;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CartMapper {

    public static CartResponse toResponse(Cart cart, Map<Integer, Product> productMap, List<Promotion> activePromotions) {
        List<CartItemResponse> itemResponses = cart.getItems().stream().map(item -> {
            Product product = productMap.get(item.getProductId());
            ProductVariant variant = product != null ? product.getVariants().stream()
                    .filter(v -> v.getId().equals(item.getVariantId()))
                    .findFirst().orElse(null) : null;

            boolean isAvailable = true;
            String message = "Sẵn sàng thanh toán";

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

            double discountAmount = 0.0;
            if (product != null && activePromotions != null) {
                discountAmount = activePromotions.stream()
                        .filter(p -> p.getTargetProductId().equals(product.getId()))
                        .map(p -> p.applyDiscount(originalPrice))
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
                    originalPrice,
                    finalPrice,
                    item.getQuantity(),
                    isAvailable,
                    message
            );
        }).collect(Collectors.toList());

        double total = itemResponses.stream()
                .filter(CartItemResponse::isAvailable)
                .mapToDouble(i -> i.getFinalPrice() * i.getQuantity())
                .sum();

        total = Math.round(total * 100.0) / 100.0;

        return new CartResponse(cart.getId(), cart.getUserId(), total, itemResponses);
    }
}