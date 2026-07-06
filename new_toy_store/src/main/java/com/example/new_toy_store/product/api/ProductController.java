package com.example.new_toy_store.product.api;

import com.example.new_toy_store.product.application.ProductService;
import com.example.new_toy_store.product.application.dto.request.ProductRequest;
import com.example.new_toy_store.product.application.dto.response.ProductResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
@Validated
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return service.getAllProducts(pageable);
    }

    @GetMapping("/category/{categoryId}")
    public Page<ProductResponse> getProductsByCategory(@PathVariable Integer categoryId, Pageable pageable) {
        return service.getProductsByCategory(categoryId, pageable);
    }

    @GetMapping("/search")
    public Page<ProductResponse> searchProducts(@RequestParam String keyword, Pageable pageable) {
        return service.searchActiveProducts(keyword, pageable);
    }

    @GetMapping("/filter")
    public Page<ProductResponse> filterProducts(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return service.filterProductsByPriceAndStatus(minPrice, maxPrice, status, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductDetails(@PathVariable Integer id) {
        return service.getProductDetails(id);
    }

    @PostMapping
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Integer id, @Valid @RequestBody ProductRequest request) {
        return service.updateInfo(id, request);
    }

    @PostMapping("/{id}/images")
    public ProductResponse addImage(
            @PathVariable Integer id,
            @RequestParam String imageUrl,
            @RequestParam(defaultValue = "false") boolean isThumbnail) {
        return service.addImage(id, imageUrl, isThumbnail);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public void removeImage(@PathVariable Integer id, @PathVariable Integer imageId) {
        service.removeImage(id, imageId);
    }

    @PatchMapping("/{productId}/variants/{variantId}/price")
    public void updateVariantPrice(
            @PathVariable Integer productId,
            @PathVariable Integer variantId,
            @RequestParam double price) {
        service.updateVariantPrice(productId, variantId, price);
    }

    @PatchMapping("/{productId}/variants/{variantId}/stock")
    public void addStock(@PathVariable Integer productId, @PathVariable Integer variantId, @RequestParam int amount) {
        service.updateStock(productId, variantId, amount);
    }

    @PatchMapping("/{productId}/images/{imageId}/thumbnail")
    public void setThumbnail(@PathVariable Integer productId, @PathVariable Integer imageId) {
        service.setThumbnail(productId, imageId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}