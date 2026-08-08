package com.example.new_toy_store.product.mapper;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.product.application.dto.request.CreateProductRequest;
import com.example.new_toy_store.product.application.dto.response.ProductEnumOptionResponse;
import com.example.new_toy_store.product.application.dto.response.ProductImageResponse;
import com.example.new_toy_store.product.application.dto.response.ProductResponse;
import com.example.new_toy_store.product.application.dto.response.ProductVariantResponse;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductAttributeValue;
import com.example.new_toy_store.product.domain.ProductImage;
import com.example.new_toy_store.product.domain.ProductStatus;
import com.example.new_toy_store.product.domain.ProductVariant;
import com.example.new_toy_store.product.domain.VariantType;
import com.example.new_toy_store.promotion.application.facade.PromotionFacade;
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

    public static ProductResponse toResponseWithPromotion(Product product, PromotionFacade promotionFacade) {
        return toProductResponse(product, promotionFacade, null);
    }

    public static ProductResponse toFullResponse(Product product, PromotionFacade promotionFacade, SupplierResponse supplier) {
        return toProductResponse(product, promotionFacade, supplier);
    }

    private static ProductResponse toProductResponse(Product product, PromotionFacade promotionFacade, SupplierResponse supplier) {
        List<ProductVariantResponse> variants = toVariantResponses(product, promotionFacade);
        ProductStatus status = product.getStatus();

        ProductResponse response = new ProductResponse(
                product.getId(),
                product.getName(),
                product.getBasePrice(),
                status.getDisplayName(),
                product.getSupplierId(),
                toCategoryIds(product),
                findThumbnailUrl(product),
                toImageResponses(product),
                product.getAverageRating(),
                product.getReviewCount(),
                product.isFeatured(),
                variants,
                toStatusOption(status),
                toStatusOptions(status.getNextValidStates()),
                determineProductActions(product, variants),
                product.isAvailableForPurchase(),
                isQuickAddAvailable(product, variants),
                findDefaultVariantId(variants),
                findDefaultVariantStockQuantity(variants)
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

    private static List<ProductImageResponse> toImageResponses(Product product) {
        return product.getImages().stream()
                .map(ProductMapper::toImageResponse)
                .collect(Collectors.toList());
    }

    private static ProductImageResponse toImageResponse(ProductImage image) {
        return new ProductImageResponse(
                image.getId(),
                image.getImageUrl(),
                image.isThumbnail()
        );
    }

    private static String findThumbnailUrl(Product product) {
        return product.getImages().stream()
                .filter(ProductImage::isThumbnail)
                .map(ProductImage::getImageUrl)
                .findFirst()
                .orElse("");
    }

    private static List<ProductVariantResponse> toVariantResponses(Product product, PromotionFacade promotionFacade) {
        return product.getVariants()
                .stream()
                .map(variant -> toVariantResponse(product.getId(), variant, promotionFacade))
                .collect(Collectors.toList());
    }

    private static ProductVariantResponse toVariantResponse(
            Integer productId,
            ProductVariant variant,
            PromotionFacade promotionFacade
    ) {
        double originalPrice = variant.getPrice();
        double discountedPrice = calculateDiscountedPrice(productId, originalPrice, promotionFacade);
        VariantType type = variant.getType();

        return new ProductVariantResponse(
                variant.getId(),
                type.getDisplayName(),
                originalPrice,
                discountedPrice,
                variant.getInventory() != null ? variant.getInventory().getStockQuantity() : 0,
                toAttributeMap(variant),
                toVariantTypeOption(type),
                toVariantTypeOptions(type.getNextValidStates()),
                determineVariantActions(variant)
        );
    }

    private static ProductEnumOptionResponse toStatusOption(ProductStatus status) {
        if (status == null) {
            return null;
        }

        return new ProductEnumOptionResponse(
                status.getCode(),
                status.name(),
                status.getDisplayName(),
                status.isVisible(),
                status.canBePurchased(),
                null
        );
    }

    private static List<ProductEnumOptionResponse> toStatusOptions(List<ProductStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }

        return statuses.stream()
                .map(ProductMapper::toStatusOption)
                .collect(Collectors.toList());
    }

    private static ProductEnumOptionResponse toVariantTypeOption(VariantType type) {
        if (type == null) {
            return null;
        }

        return new ProductEnumOptionResponse(
                type.getCode(),
                type.name(),
                type.getDisplayName(),
                null,
                null,
                type.canAddAttributes()
        );
    }

    private static List<ProductEnumOptionResponse> toVariantTypeOptions(List<VariantType> types) {
        if (types == null || types.isEmpty()) {
            return List.of();
        }

        return types.stream()
                .map(ProductMapper::toVariantTypeOption)
                .collect(Collectors.toList());
    }

    private static Map<String, String> toAttributeMap(ProductVariant variant) {
        return variant.getAttributes().stream()
                .collect(Collectors.toMap(
                        ProductAttributeValue::getAttributeName,
                        ProductAttributeValue::getAttributeValue
                ));
    }

    private static double calculateDiscountedPrice(Integer productId, double originalPrice, PromotionFacade promotionFacade) {
        double discountAmount = promotionFacade != null
                ? promotionFacade.calculateProductDiscount(productId, originalPrice)
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

    private static boolean isQuickAddAvailable(Product product, List<ProductVariantResponse> variants) {
        return product.isAvailableForPurchase()
                && variants != null
                && variants.size() == 1
                && variants.get(0).getStockQuantity() > 0;
    }

    private static Integer findDefaultVariantId(List<ProductVariantResponse> variants) {
        if (variants == null || variants.isEmpty()) {
            return null;
        }
        return variants.get(0).getId();
    }

    private static int findDefaultVariantStockQuantity(List<ProductVariantResponse> variants) {
        if (variants == null || variants.isEmpty()) {
            return 0;
        }
        return variants.get(0).getStockQuantity();
    }

    private static double roundMoney(double amount) {
        return Math.round(amount * 100.0) / 100.0;
    }
}
