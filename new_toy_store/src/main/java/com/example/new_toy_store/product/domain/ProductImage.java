package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "product_images",
        indexes = {
                @Index(name = "idx_image_product_id", columnList = "product_id")
        }
)
public class ProductImage extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private boolean isThumbnail = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    protected ProductImage() {}

    public ProductImage(String imageUrl, boolean isThumbnail) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Đường dẫn hình ảnh không được để trống");
        }
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail;
    }

    void setProduct(Product product) {
        this.product = product;
    }

    public void makeThumbnail() {
        this.isThumbnail = true;
    }

    public void removeThumbnail() {
        this.isThumbnail = false;
    }

    public Integer getId() { return id; }
    public String getImageUrl() { return imageUrl; }
    public boolean isThumbnail() { return isThumbnail; }
    public Product getProduct() { return product; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ProductImage p && id != null && id.equals(p.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}