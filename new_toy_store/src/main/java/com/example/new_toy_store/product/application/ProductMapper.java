package com.example.new_toy_store.product.application;

import com.example.new_toy_store.product.application.dto.request.ProductRequest;
import com.example.new_toy_store.product.application.dto.response.ProductResponse;
import com.example.new_toy_store.product.application.dto.response.ProductVariantResponse;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductAttributeValue;

import java.util.stream.Collectors;

public class ProductMapper {

    public static Product toEntity(ProductRequest request) {
        Product product = new Product(
                request.getName(),
                request.getBasePrice(),
                request.getCategoryId()
        );

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            request.getVariants().forEach(v ->
                    product.addRealVariant(v.getAttributes(), v.getInitialStock(), v.getPrice(), v.isMaster())
            );
        } else {
            product.addDefaultPlaceholderVariant(request.getDefaultInitialStock(), request.getBasePrice());
        }

        return product;
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBasePrice(),
                product.getCategoryId(),
                product.getVariants().stream().map(v -> new ProductVariantResponse(
                        v.getId(),
                        v.getType().name(),
                        v.getPrice(),
                        v.getInventory() != null ? v.getInventory().getStockQuantity() : 0,
                        v.getAttributes().stream().collect(Collectors.toMap(
                                ProductAttributeValue::getAttributeName,
                                ProductAttributeValue::getAttributeValue
                        ))
                )).collect(Collectors.toList())
        );
    }
}