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
import com.example.new_toy_store.product.mapper.ProductMapper;
import com.example.new_toy_store.supplier.application.SupplierService;
import com.example.new_toy_store.supplier.application.dto.response.SupplierResponse;
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
        return repository.findAll(pageable)
                .map(ProductMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductDetails(Integer id) {
        Product product = repository.findByIdWithDetails(id);
        if (product == null) {
            throw new IllegalArgumentException("Không tìm thấy sản phẩm");
        }
        return ProductMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (request.getSupplierId() != null) {
            SupplierResponse supplier = supplierService.getSupplierDetails(request.getSupplierId());
            if (!"ACTIVE".equals(supplier.getStatus())) {
                throw new IllegalStateException("Nhà cung cấp hiện đang ở trạng thái: " + supplier.getStatusDisplayName() + ". Không thể liên kết sản phẩm mới.");
            }
        }

        Set<Category> categories = new HashSet<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            categories = new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()));
            if (categories.size() != request.getCategoryIds().size()) {
                throw new IllegalArgumentException("Một hoặc nhiều ID danh mục không tồn tại trong hệ thống");
            }
        }

        Product product = ProductMapper.toEntity(request);
        product.assignSupplier(request.getSupplierId());
        categories.forEach(product::addCategory);

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            product.changeStatus(ProductStatus.from(request.getStatus()));
        }

        repository.save(product);
        return ProductMapper.toResponse(product);
    }

    @Transactional
    public ProductResponse updateInfo(Integer id, ProductRequest request) {
        Product product = getProductEntity(id);
        if (request.getSupplierId() != null && !request.getSupplierId().equals(product.getSupplierId())) {
            SupplierResponse supplier = supplierService.getSupplierDetails(request.getSupplierId());
            if (!"ACTIVE".equals(supplier.getStatus())) {
                throw new IllegalStateException("Nhà cung cấp mới đang ở trạng thái: " + supplier.getStatusDisplayName() + ". Không thể cập nhật.");
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
        return ProductMapper.toResponse(product);
    }

    @Transactional
    public void updateStock(Integer productId, Integer variantId, int amountToAdd) {
        Product product = getProductEntity(productId);
        ProductVariant variant = product.getVariants().stream()
                .filter(v -> v.getId().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy mẫu mã sản phẩm"));
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
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dữ liệu sản phẩm trên hệ thống"));

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
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
    }

    @Transactional(readOnly = true)
    public List<Product> getProductsByIdsWithDetails(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return repository.findAllByIdsWithDetails(ids);
    }

    @Transactional
    public void processImportedStock(List<ImportedStockRequest> stockUpdates) {
        Map<Integer, Integer> variantQuantities = stockUpdates.stream()
                .collect(Collectors.toMap(
                        ImportedStockRequest::getVariantId,
                        ImportedStockRequest::getQuantity,
                        Integer::sum
                ));
        this.addStockFromImport(variantQuantities);
    }

    @Transactional
    public void addStockFromImport(Map<Integer, Integer> variantQuantities) {
        if (variantQuantities == null || variantQuantities.isEmpty()) {
            return;
        }

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
}