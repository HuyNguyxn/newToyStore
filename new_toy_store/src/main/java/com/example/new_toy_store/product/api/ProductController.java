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