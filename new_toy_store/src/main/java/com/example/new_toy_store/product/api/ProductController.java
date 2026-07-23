package com.example.new_toy_store.product.api;

import com.example.new_toy_store.product.application.ProductService;
import com.example.new_toy_store.product.application.dto.request.ProductRequest;
import com.example.new_toy_store.product.application.dto.response.ProductResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
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
    public Page<ProductResponse> getProductsByCategory(
            @PathVariable @Positive(message = "ID danh mục phải lớn hơn 0") Integer categoryId,
            Pageable pageable
    ) {
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
    public ProductResponse getProductDetails(@PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer id) {
        return service.getProductDetails(id);
    }

    @PostMapping
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer id,
            @Valid @RequestBody ProductRequest request
    ) {
        return service.updateInfo(id, request);
    }

    @PostMapping("/{id}/images")
    public ProductResponse addImage(
            @PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer id,
            @RequestParam String imageUrl,
            @RequestParam(defaultValue = "false") boolean isThumbnail) {
        return service.addImage(id, imageUrl, isThumbnail);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public void removeImage(
            @PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer id,
            @PathVariable @Positive(message = "ID hình ảnh phải lớn hơn 0") Integer imageId
    ) {
        service.removeImage(id, imageId);
    }

    @PatchMapping("/{productId}/variants/{variantId}/price")
    public void updateVariantPrice(
            @PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer productId,
            @PathVariable @Positive(message = "ID biến thể phải lớn hơn 0") Integer variantId,
            @RequestParam @DecimalMin(value = "0.0", message = "Giá bán không được nhỏ hơn 0") double price) {
        service.updateVariantPrice(productId, variantId, price);
    }

    @PatchMapping("/{productId}/variants/{variantId}/stock")
    public void addStock(
            @PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer productId,
            @PathVariable @Positive(message = "ID biến thể phải lớn hơn 0") Integer variantId,
            @RequestParam @Min(value = 1, message = "Số lượng nhập kho phải lớn hơn 0") int amount
    ) {
        service.updateStock(productId, variantId, amount);
    }

    @PatchMapping("/{productId}/images/{imageId}/thumbnail")
    public void setThumbnail(
            @PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer productId,
            @PathVariable @Positive(message = "ID hình ảnh phải lớn hơn 0") Integer imageId
    ) {
        service.setThumbnail(productId, imageId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer id) {
        service.delete(id);
    }
}
