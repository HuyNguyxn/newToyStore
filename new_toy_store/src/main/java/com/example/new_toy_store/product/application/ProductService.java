package com.example.new_toy_store.product.application;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.category.domain.CategoryRepository;
import com.example.new_toy_store.product.application.dto.request.ImportedStockRequest;
import com.example.new_toy_store.product.application.dto.request.ProductRequest;
import com.example.new_toy_store.product.application.dto.response.ProductResponse;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.product.domain.ProductStatus;
import com.example.new_toy_store.product.domain.ProductVariant;
import com.example.new_toy_store.product.domain.ProductRepository;
import com.example.new_toy_store.product.domain.exception.InvalidProductOperationException;
import com.example.new_toy_store.product.domain.exception.ProductNotFoundException;
import com.example.new_toy_store.product.mapper.ProductMapper;
import com.example.new_toy_store.supplier.application.SupplierService;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;

// [ADD] Import Enum
import com.example.new_toy_store.supplier.domain.SupplierStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository repository;
    private final CategoryRepository categoryRepository;
    private final SupplierService supplierService;

    public ProductService(ProductRepository repository, CategoryRepository categoryRepository, SupplierService supplierService) {
        this.repository = repository;
        this.categoryRepository = categoryRepository;
        this.supplierService = supplierService;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> productPage = repository.findAll(pageable);
        return mapProductsToResponsesWithBatchData(productPage);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Integer categoryId, Pageable pageable) {
        Page<Product> productPage = repository.findByCategoriesId(categoryId, pageable);
        return mapProductsToResponsesWithBatchData(productPage);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchActiveProducts(String keyword, Pageable pageable) {
        Page<Product> productPage = repository.findByNameContainingIgnoreCaseAndStatus(
                keyword != null ? keyword.trim() : "", ProductStatus.ACTIVE, pageable);
        return mapProductsToResponsesWithBatchData(productPage);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> filterProductsByPriceAndStatus(Double minPrice, Double maxPrice, String status, Pageable pageable) {
        double validMinPrice = (minPrice != null && minPrice >= 0) ? minPrice : 0.0;
        double validMaxPrice = (maxPrice != null && maxPrice >= validMinPrice) ? maxPrice : Double.MAX_VALUE;
        ProductStatus targetStatus = (status != null && !status.trim().isEmpty()) ? ProductStatus.from(status) : ProductStatus.ACTIVE;

        Page<Product> productPage = repository.findByBasePriceBetweenAndStatus(validMinPrice, validMaxPrice, targetStatus, pageable);
        return mapProductsToResponsesWithBatchData(productPage);
    }

    private Page<ProductResponse> mapProductsToResponsesWithBatchData(Page<Product> productPage) {
        if (productPage.isEmpty()) {
            return Page.empty(productPage.getPageable());
        }

        Set<Integer> supplierIds = productPage.getContent().stream()
                .map(Product::getSupplierId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Integer, SupplierResponse> supplierMap = supplierService.getSuppliersByIds(supplierIds)
                .stream()
                .collect(Collectors.toMap(SupplierResponse::getId, s -> s));

        return productPage.map(product -> {
            SupplierResponse supplier = supplierMap.get(product.getSupplierId());
            return ProductMapper.toResponse(product, supplier);
        });
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductDetails(Integer id) {
        Product product = repository.findByIdWithDetails(id);
        if (product == null) {
            throw new ProductNotFoundException(id);
        }
        SupplierResponse supplier = null;
        if (product.getSupplierId() != null) {
            supplier = supplierService.getSupplierDetails(product.getSupplierId());
        }
        return ProductMapper.toResponse(product, supplier);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (request.getSupplierId() != null) {
            SupplierResponse supplier = supplierService.getSupplierDetails(request.getSupplierId());
            if (!SupplierStatus.from(supplier.getStatus()).canBeAssignedToProduct()) {
                throw InvalidProductOperationException.supplierInactive(supplier.getStatusDisplayName());
            }
        }

        Set<Category> categories = new HashSet<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
            if (categories.size() != request.getCategoryIds().size()) {
                throw InvalidProductOperationException.invalidCategories();
            }
        }

        Product product = ProductMapper.toEntity(request);
        product.assignSupplier(request.getSupplierId());
        categories.forEach(product::addCategory);

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            product.changeStatus(ProductStatus.from(request.getStatus()));
        }

        repository.save(product);
        return ProductMapper.toResponse(product, null);
    }

    @Transactional
    public ProductResponse updateInfo(Integer id, ProductRequest request) {
        Product product = getProductEntity(id);
        if (request.getSupplierId() != null && !request.getSupplierId().equals(product.getSupplierId())) {
            SupplierResponse supplier = supplierService.getSupplierDetails(request.getSupplierId());
            if (!SupplierStatus.from(supplier.getStatus()).canBeAssignedToProduct()) {
                throw InvalidProductOperationException.supplierInactive(supplier.getStatusDisplayName());
            }
            product.assignSupplier(request.getSupplierId());
        }

        product.updateInfo(request.getName(), request.getBasePrice());

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            product.changeStatus(ProductStatus.from(request.getStatus()));
        }

        if (request.getCategoryIds() != null) {
            product.getCategories().clear();
            List<Category> categories = categoryRepository.findAllById(request.getCategoryIds());
            categories.forEach(product::addCategory);
        }

        repository.save(product);
        return ProductMapper.toResponse(product, null);
    }

    @Transactional
    public void updateStock(Integer productId, Integer variantId, int amountToAdd) {
        Product product = getProductEntity(productId);
        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(InvalidProductOperationException::variantNotFound);
        variant.getInventory().addStock(amountToAdd);
    }

    @Transactional
    public void setThumbnail(Integer productId, Integer imageId) {
        Product product = getProductEntity(productId);
        product.setThumbnail(imageId);
    }

    @Transactional
    public void updateProductRating(Integer productId, double averageRating, int reviewCount) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        product.updateRatingMetrics(averageRating, reviewCount);
        repository.save(product);
    }

    @Transactional
    public void delete(Integer id) {
        Product product = getProductEntity(id);
        product.delete();
    }

    public Product getProductEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByIdsWithDetails(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return repository.findAllByIdsWithDetails(ids);
    }

    @Transactional
    public void processImportedStock(List<ImportedStockRequest> stockUpdates) {
        if (stockUpdates == null || stockUpdates.isEmpty()) return;

        Set<Integer> variantIds = stockUpdates.stream()
                .map(ImportedStockRequest::getVariantId)
                .collect(Collectors.toSet());

        Set<Integer> productIds = repository.findProductIdsByVariantIds(variantIds);

        Map<Integer, Product> productMap = repository.findAllByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (ImportedStockRequest req : stockUpdates) {
            Integer targetVariantId = req.getVariantId();
            int quantityToAdd = req.getQuantity();
            double importPrice = req.getImportPrice();

            productMap.values().stream()
                    .flatMap(p -> p.getVariants().stream())
                    .filter(v -> v.getId().equals(targetVariantId))
                    .findFirst()
                    .ifPresent(variant -> variant.importStock(quantityToAdd, importPrice));
        }
    }

    @Transactional
    public void addStockFromImport(Map<Integer, Integer> variantQuantities) {
        if (variantQuantities == null || variantQuantities.isEmpty()) return;

        Set<Integer> variantIds = variantQuantities.keySet();
        Set<Integer> productIds = repository.findProductIdsByVariantIds(variantIds);

        Map<Integer, Product> productMap = repository.findAllByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (Map.Entry<Integer, Integer> entry : variantQuantities.entrySet()) {
            Integer targetVariantId = entry.getKey();
            Integer quantityToAdd = entry.getValue();

            productMap.values().stream()
                    .flatMap(p -> p.getVariants().stream())
                    .filter(v -> v.getId().equals(targetVariantId))
                    .findFirst()
                    .ifPresent(variant -> variant.getInventory().addStock(quantityToAdd));
        }
    }

    @Transactional
    public void deductStockForOrder(Map<Integer, Integer> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) return;

        Set<Integer> variantIds = orderItems.keySet();
        Set<Integer> productIds = repository.findProductIdsByVariantIds(variantIds);

        Map<Integer, Product> productMap = repository.findAllByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (Map.Entry<Integer, Integer> entry : orderItems.entrySet()) {
            Integer targetVariantId = entry.getKey();
            Integer quantityToDeduct = entry.getValue();

            ProductVariant targetVariant = productMap.values().stream()
                    .flatMap(p -> p.getVariants().stream())
                    .filter(v -> v.getId().equals(targetVariantId))
                    .findFirst()
                    .orElseThrow(InvalidProductOperationException::variantNotFound);

            if (!targetVariant.getProduct().isAvailableForPurchase()) {
                throw InvalidProductOperationException.invalidStatus(targetVariant.getProduct().getStatus().getDisplayName());
            }

            targetVariant.getInventory().reduceStock(quantityToDeduct);
        }
    }

    @Transactional
    public void restoreStockForCancelledOrder(Map<Integer, Integer> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) return;

        Set<Integer> variantIds = orderItems.keySet();
        Set<Integer> productIds = repository.findProductIdsByVariantIds(variantIds);

        Map<Integer, Product> productMap = repository.findAllByIdsWithDetails(productIds)
                .stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        for (Map.Entry<Integer, Integer> entry : orderItems.entrySet()) {
            Integer targetVariantId = entry.getKey();
            Integer quantityToRestore = entry.getValue();

            productMap.values().stream()
                    .flatMap(p -> p.getVariants().stream())
                    .filter(v -> v.getId().equals(targetVariantId))
                    .findFirst()
                    .ifPresent(variant -> variant.getInventory().addStock(quantityToRestore));
        }
    }

    @Transactional
    public ProductResponse addImage(Integer productId, String imageUrl, boolean isThumbnail) {
        Product product = getProductEntity(productId);
        product.addImage(imageUrl, isThumbnail);
        repository.save(product);
        return ProductMapper.toResponse(product, null);
    }

    @Transactional
    public void removeImage(Integer productId, Integer imageId) {
        Product product = getProductEntity(productId);
        product.removeImage(imageId);
        repository.save(product);
    }

    @Transactional
    public void updateVariantPrice(Integer productId, Integer variantId, double newPrice) {
        Product product = getProductEntity(productId);
        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(InvalidProductOperationException::variantNotFound);
        variant.updatePrice(newPrice);
        repository.save(product);
    }
}