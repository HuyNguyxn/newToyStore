package com.example.new_toy_store.product.application.facade;

import com.example.new_toy_store.product.application.service.ProductService;
import com.example.new_toy_store.product.application.dto.request.CreateProductRequest;
import com.example.new_toy_store.product.application.dto.request.UpdateProductRequest;
import com.example.new_toy_store.product.application.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class ProductFacade {

    private final ProductService productService;

    public ProductFacade(ProductService productService) {
        this.productService = productService;
    }

    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productService.getAllProducts(pageable);
    }

    public Page<ProductResponse> getProductsByCategory(Integer categoryId, Pageable pageable) {
        return productService.getProductsByCategory(categoryId, pageable);
    }

    public Page<ProductResponse> searchActiveProducts(String keyword, Pageable pageable) {
        return productService.searchActiveProducts(keyword, pageable);
    }

    public Page<ProductResponse> filterProductsByPriceAndStatus(
            Double minPrice,
            Double maxPrice,
            String status,
            Pageable pageable
    ) {
        return productService.filterProductsByPriceAndStatus(minPrice, maxPrice, status, pageable);
    }

    public ProductResponse getProductDetails(Integer id) {
        return productService.getProductDetails(id);
    }

    public ProductResponse create(CreateProductRequest request) {
        return productService.create(request);
    }

    public ProductResponse updateInfo(Integer id, UpdateProductRequest request) {
        return productService.updateInfo(id, request);
    }

    public ProductResponse addImage(Integer id, String imageUrl, boolean thumbnail) {
        return productService.addImage(id, imageUrl, thumbnail);
    }

    public void removeImage(Integer id, Integer imageId) {
        productService.removeImage(id, imageId);
    }

    public void updateVariantPrice(Integer productId, Integer variantId, double price) {
        productService.updateVariantPrice(productId, variantId, price);
    }

    public void updateStock(Integer productId, Integer variantId, int amount) {
        productService.updateStock(productId, variantId, amount);
    }

    public void setThumbnail(Integer productId, Integer imageId) {
        productService.setThumbnail(productId, imageId);
    }

    public void delete(Integer id) {
        productService.delete(id);
    }
}
