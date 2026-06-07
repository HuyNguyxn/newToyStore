package com.example.new_toy_store.cart.mapper;

import com.example.new_toy_store.cart.application.dto.response.CartItemResponse;
import com.example.new_toy_store.cart.application.dto.response.CartResponse;
import com.example.new_toy_store.cart.domain.Cart;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductImage;
import com.example.new_toy_store.product.domain.ProductVariant;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CartMapper {

    public static CartResponse toResponse(Cart cart, Map<Integer, Product> productMap) {
        List<CartItemResponse> itemResponses = cart.getItems().stream().map(item -> {
            Product product = productMap.get(item.getProductId());
            ProductVariant variant = product != null ? product.getVariants().stream()
                    .filter(v -> v.getId().equals(item.getVariantId()))
                    .findFirst().orElse(null) : null;

            String name = product != null ? product.getName() : "Sản phẩm không tồn tại";
            String attributes = variant != null ? variant.generateAttributesSnapshot() : "";
            double price = variant != null ? variant.getPrice() : 0.0;
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
                    price,
                    item.getQuantity()
            );
        }).collect(Collectors.toList());

        double total = itemResponses.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();

        return new CartResponse(cart.getId(), cart.getUserId(), total, itemResponses);
    }
}