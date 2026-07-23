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
import com.example.new_toy_store.product.domain.ProductVariantRepository;
import com.example.new_toy_store.product.domain.exception.InvalidProductOperationException;
import com.example.new_toy_store.product.domain.exception.ProductNotFoundException;
import com.example.new_toy_store.infrastructure.specification.ProductSpecification;
import com.example.new_toy_store.product.mapper.ProductMapper;
import com.example.new_toy_store.global.event.ProductUpdatedEvent;
import com.example.new_toy_store.supplier.application.SupplierService;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
import com.example.new_toy_store.supplier.domain.SupplierStatus;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final ProductVariantRepository variantRepository;
    private final CategoryRepository categoryRepository;
    private final SupplierService supplierService;
    private final ApplicationEventPublisher eventPublisher;

    public ProductService(
            ProductRepository repository,
            ProductVariantRepository variantRepository,
            CategoryRepository categoryRepository,
            SupplierService supplierService,
            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.variantRepository = variantRepository;
        this.categoryRepository = categoryRepository;
        this.supplierService = supplierService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public ProductVariant getVariantDetail(Integer variantId) {
        return variantRepository.findByIdWithProduct(variantId)
                .orElseThrow(InvalidProductOperationException::variantNotFound);
    }

    @Transactional(readOnly = true)
    public List<ProductVariant> getVariantsByProductId(Integer productId) {
        return variantRepository.findByProductId(productId);
    }

    @Transactional
    public void processImportedStock(List<ImportedStockRequest> stockUpdates) {
        if (stockUpdates == null || stockUpdates.isEmpty()) return;

        for (ImportedStockRequest req : stockUpdates) {
            ProductVariant variant = variantRepository.findById(req.getVariantId())
                    .orElseThrow(InvalidProductOperationException::variantNotFound);

            variant.importStock(req.getQuantity(), req.getImportPrice());
        }
    }

    @Transactional
    public void addStockFromImport(Map<Integer, Integer> variantQuantities) {
        if (variantQuantities == null || variantQuantities.isEmpty()) return;

        for (Map.Entry<Integer, Integer> entry : variantQuantities.entrySet()) {
            ProductVariant variant = variantRepository.findById(entry.getKey())
                    .orElseThrow(InvalidProductOperationException::variantNotFound);

            variant.getInventory().addStock(entry.getValue());
        }
    }

    @Transactional
    public void deductStockForOrder(Map<Integer, Integer> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) return;

        for (Map.Entry<Integer, Integer> entry : orderItems.entrySet()) {
            ProductVariant targetVariant = variantRepository.findByIdWithProduct(entry.getKey())
                    .orElseThrow(InvalidProductOperationException::variantNotFound);

            if (!targetVariant.getProduct().isAvailableForPurchase()) {
                throw InvalidProductOperationException.invalidStatus(
                        targetVariant.getProduct().getStatus().getDisplayName());
            }

            targetVariant.getInventory().reduceStock(entry.getValue());
        }
    }

    @Transactional
    public void restoreStockForCancelledOrder(Map<Integer, Integer> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) return;

        for (Map.Entry<Integer, Integer> entry : orderItems.entrySet()) {
            ProductVariant variant = variantRepository.findById(entry.getKey())
                    .orElseThrow(InvalidProductOperationException::variantNotFound);

            variant.getInventory().addStock(entry.getValue());
        }
    }

    @Transactional
    public void updateStock(Integer productId, Integer variantId, int amountToAdd) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(InvalidProductOperationException::variantNotFound);
        variant.getInventory().addStock(amountToAdd);
    }

    @Transactional
    public void updateVariantPrice(Integer productId, Integer variantId, double newPrice) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(InvalidProductOperationException::variantNotFound);
        variant.updatePrice(newPrice);
        eventPublisher.publishEvent(new ProductUpdatedEvent(productId, variantId, newPrice));
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        Page<Product> productPage = repository.findAll(pageable);
        return mapProductsToResponsesWithBatchData(productPage);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Integer categoryId, Pageable pageable) {
        Specification<Product> spec = Specification.where(ProductSpecification.isDistinct())
                .and(ProductSpecification.hasCategoryId(categoryId));

        Page<Product> productPage = repository.findAll(spec, pageable);
        return mapProductsToResponsesWithBatchData(productPage);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchActiveProducts(String keyword, Pageable pageable) {
        Specification<Product> spec = Specification.where(ProductSpecification.isDistinct())
                .and(ProductSpecification.hasKeyword(keyword))
                .and(ProductSpecification.hasStatus(ProductStatus.ACTIVE));

        Page<Product> productPage = repository.findAll(spec, pageable);
        return mapProductsToResponsesWithBatchData(productPage);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> filterProductsByPriceAndStatus(Double minPrice, Double maxPrice, String status, Pageable pageable) {
        ProductStatus targetStatus = (status != null && !status.trim().isEmpty()) ? ProductStatus.from(status) : ProductStatus.ACTIVE;

        Specification<Product> spec = Specification.where(ProductSpecification.isDistinct())
                .and(ProductSpecification.priceBetween(minPrice, maxPrice))
                .and(ProductSpecification.hasStatus(targetStatus));

        Page<Product> productPage = repository.findAll(spec, pageable);
        return mapProductsToResponsesWithBatchData(productPage);
    }

    private Page<ProductResponse> mapProductsToResponsesWithBatchData(Page<Product> productPage) {
        if (productPage.isEmpty()) return Page.empty(productPage.getPageable());

        Set<Integer> supplierIds = productPage.getContent().stream()
                .map(Product::getSupplierId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Integer, SupplierResponse> supplierMap = supplierService.getSuppliersByIds(supplierIds)
                .stream().collect(Collectors.toMap(SupplierResponse::getId, s -> s));

        return productPage.map(product -> {
            SupplierResponse supplier = supplierMap.get(product.getSupplierId());
            return ProductMapper.toResponseWithSupplier(product, supplier);
        });
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductDetails(Integer id) {
        Product product = repository.findByIdWithDetails(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        SupplierResponse supplier = null;
        if (product.getSupplierId() != null) {
            supplier = supplierService.getSupplierDetails(product.getSupplierId());
        }
        return ProductMapper.toResponseWithSupplier(product, supplier);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        SupplierResponse supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierService.getSupplierDetails(request.getSupplierId());
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
        return ProductMapper.toResponseWithSupplier(product, supplier);
    }

    @Transactional
    public ProductResponse updateInfo(Integer id, ProductRequest request) {
        Product product = getProductEntity(id);
        SupplierResponse supplier = null;

        if (request.getSupplierId() != null) {
            supplier = supplierService.getSupplierDetails(request.getSupplierId());
            if (!request.getSupplierId().equals(product.getSupplierId())) {
                if (!SupplierStatus.from(supplier.getStatus()).canBeAssignedToProduct()) {
                    throw InvalidProductOperationException.supplierInactive(supplier.getStatusDisplayName());
                }
                product.assignSupplier(request.getSupplierId());
            }
        } else if (product.getSupplierId() != null) {
            supplier = supplierService.getSupplierDetails(product.getSupplierId());
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
        return ProductMapper.toResponseWithSupplier(product, supplier);
    }

    @Transactional
    public void setThumbnail(Integer productId, Integer imageId) {
        getProductEntity(productId).setThumbnail(imageId);
    }

    @Transactional
    public void updateProductRating(Integer productId, double averageRating, int reviewCount) {
        Product product = getProductEntity(productId);
        product.updateRatingMetrics(averageRating, reviewCount);
        repository.save(product);
    }

    @Transactional
    public void delete(Integer id) {
        getProductEntity(id).delete();
    }

    public Product getProductEntity(Integer id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByIdsWithDetails(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return repository.findAllByIdsWithDetails(ids);
    }

    @Transactional
    public ProductResponse addImage(Integer productId, String imageUrl, boolean isThumbnail) {
        Product product = getProductEntity(productId);
        product.addImage(imageUrl, isThumbnail);
        repository.save(product);

        SupplierResponse supplier = product.getSupplierId() != null ?
                supplierService.getSupplierDetails(product.getSupplierId()) : null;
        return ProductMapper.toResponseWithSupplier(product, supplier);
    }

    @Transactional
    public void removeImage(Integer productId, Integer imageId) {
        Product product = getProductEntity(productId);
        product.removeImage(imageId);
        repository.save(product);
    }
}
