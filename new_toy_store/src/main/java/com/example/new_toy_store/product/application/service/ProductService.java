package com.example.new_toy_store.product.application.service;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.category.application.facade.CategoryFacade;
import com.example.new_toy_store.product.application.dto.request.CreateProductRequest;
import com.example.new_toy_store.product.application.dto.request.ImportedStockRequest;
import com.example.new_toy_store.product.application.dto.request.ProductVariantRequest;
import com.example.new_toy_store.product.application.dto.request.UpdateProductRequest;
import com.example.new_toy_store.product.application.dto.request.UpdateProductStatusRequest;
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
import com.example.new_toy_store.supplier.application.facade.SupplierFacade;
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
    private final CategoryFacade categoryFacade;
    private final SupplierFacade supplierFacade;
    private final ApplicationEventPublisher eventPublisher;

    public ProductService(
            ProductRepository repository,
            ProductVariantRepository variantRepository,
            CategoryFacade categoryFacade,
            SupplierFacade supplierFacade,
            ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.variantRepository = variantRepository;
        this.categoryFacade = categoryFacade;
        this.supplierFacade = supplierFacade;
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

            if (variant.hasImportedBatch(req.getBatchNumber())) {
                continue;
            }
            variant.importStock(req.getQuantity(), req.getImportPrice(), req.getBatchNumber());
            if (req.getSellingPrice() != null) {
                variant.updatePrice(req.getSellingPrice());
                eventPublisher.publishEvent(new ProductUpdatedEvent(
                        req.getProductId(), req.getVariantId(), req.getSellingPrice()));
            }
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
    public Page<ProductResponse> filterProducts(String keyword, Integer categoryId, Double minPrice, Double maxPrice, String status, Boolean featured, Pageable pageable) {
        ProductStatus targetStatus = (status != null && !status.trim().isEmpty()) ? ProductStatus.from(status) : null;

        Specification<Product> spec = Specification.where(ProductSpecification.isDistinct());

        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and(ProductSpecification.hasKeyword(keyword.trim()));
        }
        if (categoryId != null) {
            java.util.List<Integer> allCategoryIds = new java.util.ArrayList<>();
            allCategoryIds.add(categoryId);
            allCategoryIds.addAll(categoryFacade.getAllSubCategoryIds(categoryId));
            spec = spec.and(ProductSpecification.hasCategoryIds(allCategoryIds));
        }
        if (minPrice != null || maxPrice != null) {
            spec = spec.and(ProductSpecification.priceBetween(minPrice, maxPrice));
        }
        if (targetStatus != null) {
            spec = spec.and(ProductSpecification.hasStatus(targetStatus));
        }
        if (featured != null) {
            spec = spec.and(ProductSpecification.isFeatured(featured));
        }

        Page<Product> productPage = repository.findAll(spec, pageable);
        return mapProductsToResponsesWithBatchData(productPage);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> filterProducts(String keyword, Integer categoryId, Double minPrice, Double maxPrice, String status, Pageable pageable) {
        return filterProducts(keyword, categoryId, minPrice, maxPrice, status, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> filterProductsByPriceAndStatus(Double minPrice, Double maxPrice, String status, Pageable pageable) {
        return filterProducts(null, null, minPrice, maxPrice, status, pageable);
    }

    private Page<ProductResponse> mapProductsToResponsesWithBatchData(Page<Product> productPage) {
        if (productPage.isEmpty()) return Page.empty(productPage.getPageable());

        Set<Integer> supplierIds = productPage.getContent().stream()
                .map(Product::getSupplierId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Map<Integer, SupplierResponse> supplierMap = supplierFacade.getSuppliersByIds(supplierIds)
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
            supplier = supplierFacade.getSupplierDetails(product.getSupplierId());
        }
        return ProductMapper.toResponseWithSupplier(product, supplier);
    }

    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        SupplierResponse supplier = null;
        if (request.getSupplierId() != null) {
            supplier = supplierFacade.getSupplierDetails(request.getSupplierId());
            if (!supplier.getStatus().canBeAssignedToProduct()) {
                throw InvalidProductOperationException.supplierInactive(supplier.getStatusDisplayName());
            }
        }

        Set<Category> categories = new HashSet<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            categories = new HashSet<>(categoryFacade.getExistingCategories(new HashSet<>(request.getCategoryIds())));
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
    public ProductResponse addVariant(Integer productId, ProductVariantRequest request) {
        Product product = getProductEntity(productId);
        Map<String, String> attrs = (request.getAttributes() != null && !request.getAttributes().isEmpty())
                ? request.getAttributes()
                : Map.of("Biến thể", "Phiên bản mới");

        product.addRealVariant(
                attrs,
                request.getInitialStock(),
                request.getPrice() > 0 ? request.getPrice() : product.getBasePrice(),
                request.isMaster()
        );
        repository.save(product);

        SupplierResponse supplier = null;
        if (product.getSupplierId() != null) {
            supplier = supplierFacade.getSupplierDetails(product.getSupplierId());
        }
        return ProductMapper.toResponseWithSupplier(product, supplier);
    }

    @Transactional
    public ProductResponse updateInfo(Integer id, UpdateProductRequest request) {
        Product product = getProductEntity(id);
        SupplierResponse supplier = null;

        if (request.getSupplierId() != null) {
            supplier = supplierFacade.getSupplierDetails(request.getSupplierId());
            if (!request.getSupplierId().equals(product.getSupplierId())) {
                if (!supplier.getStatus().canBeAssignedToProduct()) {
                    throw InvalidProductOperationException.supplierInactive(supplier.getStatusDisplayName());
                }
                product.assignSupplier(request.getSupplierId());
            }
        } else if (product.getSupplierId() != null) {
            supplier = supplierFacade.getSupplierDetails(product.getSupplierId());
        }

        product.updateInfo(request.getName(), request.getBasePrice());

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            product.changeStatus(ProductStatus.from(request.getStatus()));
        }

        if (request.getCategoryIds() != null) {
            product.getCategories().clear();
            List<Category> categories = categoryFacade.getExistingCategories(new HashSet<>(request.getCategoryIds()));
            categories.forEach(product::addCategory);
        }

        repository.save(product);
        return ProductMapper.toResponseWithSupplier(product, supplier);
    }

    @Transactional
    public ProductResponse updateStatus(Integer id, UpdateProductStatusRequest request) {
        Product product = getProductEntity(id);
        product.changeStatus(ProductStatus.from(request.getStatus()));
        repository.save(product);

        SupplierResponse supplier = product.getSupplierId() != null
                ? supplierFacade.getSupplierDetails(product.getSupplierId())
                : null;
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
                supplierFacade.getSupplierDetails(product.getSupplierId()) : null;
        return ProductMapper.toResponseWithSupplier(product, supplier);
    }

    @Transactional
    public void removeImage(Integer productId, Integer imageId) {
        Product product = getProductEntity(productId);
        product.removeImage(imageId);
        repository.save(product);
    }

    @Transactional
    public ProductResponse toggleFeatured(Integer productId) {
        Product product = getProductEntity(productId);
        product.toggleFeatured();
        repository.save(product);

        SupplierResponse supplier = product.getSupplierId() != null ?
                supplierFacade.getSupplierDetails(product.getSupplierId()) : null;
        return ProductMapper.toResponseWithSupplier(product, supplier);
    }
}
