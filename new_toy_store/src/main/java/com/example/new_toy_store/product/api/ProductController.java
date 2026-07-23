package com.example.new_toy_store.product.api;

import com.example.new_toy_store.product.application.facade.ProductFacade;
import com.example.new_toy_store.product.application.dto.request.AddProductImageRequest;
import com.example.new_toy_store.product.application.dto.request.AddVariantStockRequest;
import com.example.new_toy_store.product.application.dto.request.CreateProductRequest;
import com.example.new_toy_store.product.application.dto.request.UpdateProductRequest;
import com.example.new_toy_store.product.application.dto.request.UpdateVariantPriceRequest;
import com.example.new_toy_store.product.application.dto.response.ProductResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@Validated
public class ProductController {

    private final ProductFacade facade;

    public ProductController(ProductFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return facade.getAllProducts(pageable);
    }

    @GetMapping("/category/{categoryId}")
    public Page<ProductResponse> getProductsByCategory(
            @PathVariable @Positive(message = "ID danh mục phải lớn hơn 0") Integer categoryId,
            Pageable pageable
    ) {
        return facade.getProductsByCategory(categoryId, pageable);
    }

    @GetMapping("/search")
    public Page<ProductResponse> searchProducts(@RequestParam String keyword, Pageable pageable) {
        return facade.searchActiveProducts(keyword, pageable);
    }

    @GetMapping("/filter")
    public Page<ProductResponse> filterProducts(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        return facade.filterProductsByPriceAndStatus(minPrice, maxPrice, status, pageable);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductDetails(
            @PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer id
    ) {
        return facade.getProductDetails(id);
    }

    @PostMapping
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        return facade.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(
            @PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer id,
            @Valid @RequestBody UpdateProductRequest request
    ) {
        return facade.updateInfo(id, request);
    }

    @PostMapping("/{id}/images")
    public ProductResponse addImage(
            @PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer id,
            @Valid @RequestBody AddProductImageRequest request
    ) {
        return facade.addImage(id, request.getImageUrl(), request.isThumbnail());
    }

    @DeleteMapping("/{id}/images/{imageId}")
    public void removeImage(
            @PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer id,
            @PathVariable @Positive(message = "ID hình ảnh phải lớn hơn 0") Integer imageId
    ) {
        facade.removeImage(id, imageId);
    }

    @PatchMapping("/{productId}/variants/{variantId}/price")
    public void updateVariantPrice(
            @PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer productId,
            @PathVariable @Positive(message = "ID biến thể phải lớn hơn 0") Integer variantId,
            @Valid @RequestBody UpdateVariantPriceRequest request
    ) {
        facade.updateVariantPrice(productId, variantId, request.getPrice());
    }

    @PatchMapping("/{productId}/variants/{variantId}/stock")
    public void addStock(
            @PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer productId,
            @PathVariable @Positive(message = "ID biến thể phải lớn hơn 0") Integer variantId,
            @Valid @RequestBody AddVariantStockRequest request
    ) {
        facade.updateStock(productId, variantId, request.getAmount());
    }

    @PatchMapping("/{productId}/images/{imageId}/thumbnail")
    public void setThumbnail(
            @PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer productId,
            @PathVariable @Positive(message = "ID hình ảnh phải lớn hơn 0") Integer imageId
    ) {
        facade.setThumbnail(productId, imageId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @Positive(message = "ID sản phẩm phải lớn hơn 0") Integer id) {
        facade.delete(id);
    }
}
