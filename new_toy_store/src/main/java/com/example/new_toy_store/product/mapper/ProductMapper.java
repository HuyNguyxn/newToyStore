package com.example.new_toy_store.product.mapper;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.product.application.dto.request.CreateProductRequest;
import com.example.new_toy_store.product.application.dto.response.ProductResponse;
import com.example.new_toy_store.product.application.dto.response.ProductVariantResponse;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductAttributeValue;
import com.example.new_toy_store.product.domain.ProductStatus;
import com.example.new_toy_store.product.domain.ProductVariant;
import com.example.new_toy_store.product.domain.VariantType;
import com.example.new_toy_store.promotion.application.PromotionService;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ProductMapper {

    private ProductMapper() {
    }

    public static Product toEntity(CreateProductRequest request) {
        Product product = new Product(
                request.getName(),
                request.getBasePrice()
        );

        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            request.getVariants().forEach(variantRequest ->
                    product.addRealVariant(
                            variantRequest.getAttributes(),
                            variantRequest.getInitialStock(),
                            variantRequest.getPrice(),
                            variantRequest.isMaster()
                    )
            );
        } else {
            product.addDefaultPlaceholderVariant(request.getDefaultInitialStock(), request.getBasePrice());
        }

        return product;
    }

    public static ProductResponse toResponse(Product product) {
        return toProductResponse(product, null, null);
    }

    public static ProductResponse toResponseWithSupplier(Product product, SupplierResponse supplier) {
        return toProductResponse(product, null, supplier);
    }

    public static ProductResponse toResponseWithPromotion(Product product, PromotionService promotionService) {
        return toProductResponse(product, promotionService, null);
    }

    public static ProductResponse toFullResponse(Product product, PromotionService promotionService, SupplierResponse supplier) {
        return toProductResponse(product, promotionService, supplier);
    }

    private static ProductResponse toProductResponse(Product product, PromotionService promotionService, SupplierResponse supplier) {
        List<ProductVariantResponse> variants = toVariantResponses(product, promotionService);
        ProductStatus status = product.getStatus();

        ProductResponse response = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBasePrice(),
                status.getDisplayName(),
                product.getSupplierId(),
                toCategoryIds(product),
                product.getAverageRating(),
                product.getReviewCount(),
                variants,
                status,
                status.getNextValidStates(),
                determineProductActions(product, variants)
        );

        if (supplier != null) {
            response.setSupplierName(supplier.getName());
        }

        return response;
    }

    private static List<Integer> toCategoryIds(Product product) {
        return product.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toList());
    }

    private static List<ProductVariantResponse> toVariantResponses(Product product, PromotionService promotionService) {
        return product.getVariants()
                .stream()
                .map(variant -> toVariantResponse(product.getId(), variant, promotionService))
                .collect(Collectors.toList());
    }

    private static ProductVariantResponse toVariantResponse(
            Integer productId,
            ProductVariant variant,
            PromotionService promotionService
    ) {
        double originalPrice = variant.getPrice();
        double discountedPrice = calculateDiscountedPrice(productId, originalPrice, promotionService);
        VariantType type = variant.getType();

        return new ProductVariantResponse(
                variant.getId(),
                type.getDisplayName(),
                originalPrice,
                discountedPrice,
                variant.getInventory() != null ? variant.getInventory().getStockQuantity() : 0,
                toAttributeMap(variant),
                type,
                type.getNextValidStates(),
                determineVariantActions(variant)
        );
    }

    private static Map<String, String> toAttributeMap(ProductVariant variant) {
        return variant.getAttributes().stream()
                .collect(Collectors.toMap(
                        ProductAttributeValue::getAttributeName,
                        ProductAttributeValue::getAttributeValue
                ));
    }

    private static double calculateDiscountedPrice(Integer productId, double originalPrice, PromotionService promotionService) {
        double discountAmount = promotionService != null
                ? promotionService.calculateProductDiscount(productId, originalPrice)
                : 0.0;
        return roundMoney(Math.max(0.0, originalPrice - discountAmount));
    }

    private static List<String> determineProductActions(Product product, List<ProductVariantResponse> variants) {
        ProductStatus status = product.getStatus();
        if (status == ProductStatus.ACTIVE) {
            return List.of("UPDATE_PRODUCT", "ADD_VARIANT", "UPDATE_PRICE", "UPDATE_STOCK", "SET_INACTIVE");
        }
        if (status == ProductStatus.OUT_OF_STOCK) {
            return List.of("UPDATE_PRODUCT", "UPDATE_STOCK", "SET_ACTIVE", "SET_INACTIVE");
        }
        return List.of("UPDATE_PRODUCT", "SET_ACTIVE");
    }

    private static List<String> determineVariantActions(ProductVariant variant) {
        if (variant.getType() == VariantType.DEFAULT) {
            return List.of("UPDATE_PRICE", "UPDATE_STOCK");
        }
        if (variant.getType() == VariantType.MASTER) {
            return List.of("UPDATE_PRICE", "UPDATE_STOCK", "SET_REGULAR");
        }
        return List.of("UPDATE_PRICE", "UPDATE_STOCK", "SET_MASTER");
    }

    private static double roundMoney(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}
