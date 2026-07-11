package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import com.example.new_toy_store.product.domain.exception.InvalidProductOperationException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "inventories",
        indexes = {
                @Index(name = "idx_inventory_variant_id", columnList = "variant_id")
        }
)
public class Inventory extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    protected Inventory() {}

    public Inventory(int initialStock) {
        if (initialStock < 0) {
            throw InvalidProductOperationException.negativeInitialStock();
        }
        this.stockQuantity = initialStock;
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    public void addStock(int amount) {
        if (amount <= 0) {
            throw InvalidProductOperationException.invalidStockAmount();
        }
        this.stockQuantity += amount;
    }

    public void reduceStock(int amount) {
        if (amount <= 0) {
            throw InvalidProductOperationException.invalidStockAmount();
        }
        if (this.stockQuantity < amount) {
            throw InvalidProductOperationException.insufficientStock();
        }
        this.stockQuantity -= amount;
    }

    public Integer getId() { return id; }
    public int getStockQuantity() { return stockQuantity; }
    public ProductVariant getVariant() { return variant; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Inventory i && id != null && id.equals(i.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}