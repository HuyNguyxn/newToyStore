package com.example.new_toy_store.product.mapper;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.product.application.dto.request.ProductRequest;
import com.example.new_toy_store.product.application.dto.response.ProductResponse;
import com.example.new_toy_store.product.application.dto.response.ProductVariantResponse;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductAttributeValue;

import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {

    public static Product toEntity(ProductRequest request) {
        Product product = new Product(
                request.getName(),
                request.getBasePrice()
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
        List<Integer> categoryIds = product.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toList());
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBasePrice(),
                product.getStatus().name(),
                product.getSupplierId(),
                categoryIds,
                product.getVariants().stream().map(variant -> new ProductVariantResponse(
                        variant.getId(),
                        variant.getType().name(),
                        variant.getPrice(),
                        variant.getInventory() != null ? variant.getInventory().getStockQuantity() : 0,
                        variant.getAttributes().stream().collect(Collectors.toMap(
                                ProductAttributeValue::getAttributeName,
                                ProductAttributeValue::getAttributeValue
                        ))
                )).collect(Collectors.toList())
        );
    }
}