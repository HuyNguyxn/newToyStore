package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.global.common.BaseRootEntity;
import com.example.new_toy_store.product.domain.exception.InvalidProductOperationException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_status", columnList = "status"),
                @Index(name = "idx_product_created_at", columnList = "created_at"),
                @Index(name = "idx_product_supplier_id", columnList = "supplier_id")
        }
)
public class Product extends BaseRootEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @Column(name = "supplier_id")
    private Integer supplierId;

    @Column(name = "average_rating", nullable = false)
    private double averageRating = 0.0;

    @Column(name = "review_count", nullable = false)
    private int reviewCount = 0;

    @ManyToMany
    @JoinTable(
            name = "product_categories",
            joinColumns = @JoinColumn(name = "product_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();

    protected Product() {}

    public Product(String name, double basePrice) {
        if (name == null || name.trim().isEmpty()) throw InvalidProductOperationException.emptyName();
        if (basePrice < 0) throw InvalidProductOperationException.negativePrice();
        this.name = name; this.basePrice = basePrice;
    }

    public void assignSupplier(Integer supplierId) { this.supplierId = supplierId; }
    public void updateInfo(String name, double basePrice) { if (name != null && !name.trim().isEmpty()) this.name = name; if (basePrice >= 0) this.basePrice = basePrice; }
    public void changeStatus(ProductStatus newStatus) { if (newStatus != null) this.status = newStatus; }
    public void addCategory(Category category) { if (category != null) this.categories.add(category); }
    public void removeCategory(Category category) { if (category != null) this.categories.remove(category); }

    public void addDefaultPlaceholderVariant(int initialStock, double price) {
        ProductVariant variant = ProductVariant.createDefaultPlaceholder(initialStock, price);
        variant.setProduct(this);
        this.variants.add(variant);
    }

    public void addRealVariant(Map<String, String> attributeMap, int initialStock, double price, boolean setAsMaster) {
        ProductVariant variant = ProductVariant.createRealVariant(initialStock, price, setAsMaster);
        attributeMap.forEach(variant::addAttribute);
        variant.setProduct(this);
        this.variants.add(variant);
    }

    public void setThumbnail(Integer imageId) {
        boolean imageFound = false;
        for (ProductImage image : this.images) {
            if (image.getId() != null && image.getId().equals(imageId)) {
                image.makeThumbnail(); imageFound = true;
            } else { image.removeThumbnail(); }
        }
        if (!imageFound) throw InvalidProductOperationException.invalidImage(imageId);
    }

    public void updateRatingMetrics(double averageRating, int reviewCount) {
        this.averageRating = Math.max(0.0, averageRating);
        this.reviewCount = Math.max(0, reviewCount);
    }

    public double getAverageRating() { return averageRating; }
    public int getReviewCount() { return reviewCount; }
    public boolean isAvailableForPurchase() { return this.status != null && this.status.canBePurchased(); }
    public boolean isVisibleToCustomers() { return this.status != null && this.status.isVisible(); }

    public void addImage(String imageUrl, boolean isThumbnail) {
        ProductImage image = new ProductImage(imageUrl, isThumbnail);
        if (isThumbnail || this.images.isEmpty()) {
            this.images.forEach(ProductImage::removeThumbnail);
            image.makeThumbnail();
        }
        image.setProduct(this);
        this.images.add(image);
    }

    public void removeImage(Integer imageId) {
        boolean removed = this.images.removeIf(img -> img.getId() != null && img.getId().equals(imageId));
        if (!removed) throw InvalidProductOperationException.invalidImage(imageId);
        if (!this.images.isEmpty() && this.images.stream().noneMatch(ProductImage::isThumbnail)) this.images.get(0).makeThumbnail();
    }

    @Override
    public void delete() {
        super.delete();
        this.images.forEach(ProductImage::delete);
        this.variants.forEach(ProductVariant::delete);
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public ProductStatus getStatus() { return status; }
    public Integer getSupplierId() { return supplierId; }
    public Set<Category> getCategories() { return Collections.unmodifiableSet(categories); }
    public List<ProductImage> getImages() { return Collections.unmodifiableList(images); }
    public List<ProductVariant> getVariants() { return Collections.unmodifiableList(variants); }

    @Override public boolean equals(Object o) { return this == o || (o instanceof Product p && id != null && id.equals(p.id)); }
    @Override public int hashCode() { return getClass().hashCode(); }
}