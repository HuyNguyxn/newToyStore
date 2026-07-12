package com.example.new_toy_store.product.mapper;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.product.application.dto.request.ProductRequest;
import com.example.new_toy_store.product.application.dto.response.ProductResponse;
import com.example.new_toy_store.product.application.dto.response.ProductVariantResponse;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductAttributeValue;
import com.example.new_toy_store.promotion.application.PromotionService;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;

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
        return buildResponse(product, null, null);
    }

    public static ProductResponse toResponseWithSupplier(Product product, SupplierResponse supplier) {
        return buildResponse(product, null, supplier);
    }

    public static ProductResponse toResponseWithPromotion(Product product, PromotionService promotionService) {
        return buildResponse(product, promotionService, null);
    }

    public static ProductResponse toFullResponse(Product product, PromotionService promotionService, SupplierResponse supplier) {
        return buildResponse(product, promotionService, supplier);
    }

    private static ProductResponse buildResponse(Product product, PromotionService promotionService, SupplierResponse supplier) {
        List<Integer> categoryIds = product.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        ProductResponse response = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBasePrice(),
                product.getStatus().getDisplayName(),
                product.getSupplierId(),
                categoryIds,
                product.getAverageRating(),
                product.getReviewCount(),
                product.getVariants().stream().map(variant -> {
                    double originalPrice = variant.getPrice();
                    double discountAmount = promotionService != null ?
                            promotionService.calculateProductDiscount(product.getId(), originalPrice) : 0.0;
                    double discountedPrice = Math.max(0.0, Math.round((originalPrice - discountAmount) * 100.0) / 100.0);

                    return new ProductVariantResponse(
                            variant.getId(),
                            variant.getType().getDisplayName(),
                            originalPrice,
                            discountedPrice,
                            variant.getInventory() != null ? variant.getInventory().getStockQuantity() : 0,
                            variant.getAttributes().stream().collect(Collectors.toMap(
                                    ProductAttributeValue::getAttributeName,
                                    ProductAttributeValue::getAttributeValue
                            ))
                    );
                }).collect(Collectors.toList())
        );

        if (supplier != null) {
            response.setSupplierName(supplier.getName());
        }

        return response;
    }
}