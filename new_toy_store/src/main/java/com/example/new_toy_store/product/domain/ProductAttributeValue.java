package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "product_attribute_values")
@SQLRestriction("deleted_at IS NULL")
public class ProductAttributeValue extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String attributeName;

    @Column(nullable = false)
    private String attributeValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    protected ProductAttributeValue() {}

    public ProductAttributeValue(String attributeName, String attributeValue) {
        if (attributeName == null || attributeName.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute name is required");
        }
        if (attributeValue == null || attributeValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute value is required");
        }
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

    void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    public Integer getId() { return id; }
    public String getAttributeName() { return attributeName; }
    public String getAttributeValue() { return attributeValue; }
    public ProductVariant getVariant() { return variant; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ProductAttributeValue p && id != null && id.equals(p.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}