package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.global.common.BaseAuditEntity;
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
                @Index(name = "idx_product_created_at", columnList = "created_at")
        }
)
public class Product extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double basePrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

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
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (basePrice < 0) {
            throw new IllegalArgumentException("Base price cannot be negative");
        }
        this.name = name;
        this.basePrice = basePrice;
    }

    public void updateInfo(String name, double basePrice) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (basePrice >= 0) {
            this.basePrice = basePrice;
        }
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public void setStatus(ProductStatus status) {
        if (status != null) {
            this.status = status;
        }
    }

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
                image.makeThumbnail();
                imageFound = true;
            } else {
                image.removeThumbnail();
            }
        }

        if (!imageFound) {
            throw new IllegalArgumentException("Image ID " + imageId + " does not belong to this product");
        }
    }

    public boolean isAvailableForPurchase() {
        return this.status != null && this.status.canBePurchased();
    }

    public boolean isVisibleToCustomers() {
        return this.status != null && this.status.isVisible();
    }

    @Override
    public void delete() {
        super.delete();
        this.images.forEach(BaseAuditEntity::delete);
        this.variants.forEach(BaseAuditEntity::delete);
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public double getBasePrice() { return basePrice; }
    public ProductStatus getStatus() { return status; }
    public Set<Category> getCategories() { return Collections.unmodifiableSet(categories); }
    public List<ProductImage> getImages() { return Collections.unmodifiableList(images); }
    public List<ProductVariant> getVariants() { return Collections.unmodifiableList(variants); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Product p && id != null && id.equals(p.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}