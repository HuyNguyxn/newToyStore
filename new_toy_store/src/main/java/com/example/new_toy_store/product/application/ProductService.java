package com.example.new_toy_store.product.application;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.category.domain.CategoryRepository;
import com.example.new_toy_store.product.application.dto.request.ProductRequest;
import com.example.new_toy_store.product.application.dto.response.ProductResponse;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductStatus;
import com.example.new_toy_store.product.domain.ProductVariant;
import com.example.new_toy_store.product.domain.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository repository, CategoryRepository categoryRepository) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return repository.findAll(pageable)
                .map(ProductMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductDetails(Integer id) {
        Product product = repository.findByIdWithDetails(id);
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        return ProductMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = ProductMapper.toEntity(request);
        if (request.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(request.getStatus().toUpperCase()));
        }

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            if (categories.isEmpty()) {
                throw new RuntimeException("Categories not found");
            }
            product.setCategories(new HashSet<>(categories));
        }

        repository.save(product);
        return ProductMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse updateInfo(Integer id, ProductRequest request) {
        Product product = getProductEntity(id);
        product.updateInfo(request.getName(), request.getBasePrice());

        if (request.getStatus() != null) {
            product.setStatus(ProductStatus.valueOf(request.getStatus().toUpperCase()));
        }

        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            product.setCategories(new HashSet<>(categories));
        } else {
            product.getCategories().clear();
        }

        return ProductMapper.toResponse(product);
    }

    @Transactional
    public void updateStock(Integer productId, Integer variantId, int amountToAdd) {
        Product product = getProductEntity(productId);
        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Variant not found"));
        variant.getInventory().addStock(amountToAdd);
    }

    @Transactional
    public void setThumbnail(Integer productId, Integer imageId) {
        Product product = getProductEntity(productId);
        product.setThumbnail(imageId);
    }

    @Transactional
    public void delete(Integer id) {
        Product product = getProductEntity(id);
        product.delete();
    }

    public Product getProductEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
}