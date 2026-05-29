package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "inventories")
@SQLRestriction("deleted_at IS NULL")
public class Inventory extends BaseAuditEntity {

    @Id
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "variant_id")
    private VariantType variant;

    @Column(nullable = false)
    private int stockQuantity;

    protected Inventory() {}

    public Inventory(int initialStock) {
        if (initialStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        this.stockQuantity = initialStock;
    }

    void setVariant(VariantType variant) {
        this.variant = variant;
    }

    public void addStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.stockQuantity += amount;
    }

    public void reduceStock(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (this.stockQuantity < amount) {
            throw new IllegalStateException("Insufficient stock");
        }
        this.stockQuantity -= amount;
    }

    public Integer getId() { return id; }
    public VariantType getVariant() { return variant; }
    public int getStockQuantity() { return stockQuantity; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Inventory i && id != null && id.equals(i.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}