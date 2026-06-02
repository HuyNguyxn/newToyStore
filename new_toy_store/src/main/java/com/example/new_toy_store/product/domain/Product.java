package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.category.domain.Category;
import com.example.new_toy_store.global.common.BaseAuditEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.*;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "products",
        indexes = {
                @Index(name = "idx_product_category", columnList = "category_id")
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

    public void addImage(String imageUrl, boolean isThumbnail) {
        ProductImage image = new ProductImage(imageUrl, isThumbnail);
        if (isThumbnail) {
            this.images.forEach(ProductImage::removeThumbnail);
        } else if (this.images.isEmpty()) {
            image.makeThumbnail();
        }
        image.setProduct(this);
        this.images.add(image);
    }

    public void setThumbnail(Integer imageId) {
        boolean found = false;
        for (ProductImage img : this.images) {
            if (img.getId().equals(imageId)) {
                img.makeThumbnail();
                found = true;
            } else {
                img.removeThumbnail();
            }
        }
        if (!found) throw new IllegalArgumentException("Image not found");
    }

    public void removeImage(ProductImage image) {
        if (image != null && this.images.contains(image)) {
            image.setProduct(null);
            this.images.remove(image);
        }
    }

    public void addDefaultPlaceholderVariant(int initialStock, double price) {
        if (!this.variants.isEmpty()) {
            throw new IllegalStateException("Cannot add default placeholder if product already has variants");
        }
        ProductVariant placeholder = ProductVariant.createDefaultPlaceholder(initialStock, price);
        placeholder.setProduct(this);
        this.variants.add(placeholder);
    }

    public void addRealVariant(Map<String, String> attributeMap, int initialStock, double price, boolean setAsMaster) {
        if (attributeMap == null || attributeMap.isEmpty()) {
            throw new IllegalArgumentException("Real variants must have attributes");
        }

        if (!this.variants.isEmpty() && this.variants.get(0).getType() == VariantType.DEFAULT) {
            throw new IllegalStateException("Cannot mix real variants with default placeholder");
        }

        if (setAsMaster) {
            this.variants.forEach(v -> {
                if (v.getType() == VariantType.MASTER) {
                    v.makeRegular();
                }
            });
        } else if (this.variants.isEmpty()) {
            setAsMaster = true;
        }

        ProductVariant variant = ProductVariant.createRealVariant(initialStock, price, setAsMaster);
        attributeMap.forEach(variant::addAttribute);
        variant.setProduct(this);
        this.variants.add(variant);
    }

    public void changeMasterVariant(Integer variantId) {
        boolean found = false;
        for (ProductVariant v : this.variants) {
            if (v.getId().equals(variantId)) {
                v.makeMaster();
                found = true;
            } else if (v.getType() == VariantType.MASTER) {
                v.makeRegular();
            }
        }
        if (!found) throw new IllegalArgumentException("Variant not found in this product");
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public void setStatus(ProductStatus status) {
        if (status != null) {
            this.status = status;
        }
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