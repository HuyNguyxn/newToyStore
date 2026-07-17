package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.global.common.BaseSoftDeleteEntity;
import com.example.new_toy_store.product.domain.exception.InvalidProductOperationException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "inventories",
        indexes = {@Index(name = "idx_inventory_variant_id", columnList = "variant_id")}
)
public class Inventory extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @OneToMany(mappedBy = "inventory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<InventoryBatch> batches = new ArrayList<>();

    protected Inventory() {
    }

    public Inventory(int initialStock) {
        if (initialStock < 0) {
            throw InvalidProductOperationException.negativeInitialStock();
        }
        this.stockQuantity = initialStock;
        if (initialStock > 0) {
            InventoryBatch defaultBatch = new InventoryBatch("DEFAULT_BATCH", LocalDate.now().plusYears(5), initialStock, this);
            this.batches.add(defaultBatch);
        }
    }

    public void setVariant(ProductVariant variant) {
        this.variant = variant;
    }

    public void addStock(int amount, String batchNumber, LocalDate expiryDate) {
        if (amount <= 0) {
            throw InvalidProductOperationException.invalidStockAmount();
        }

        InventoryBatch targetBatch = this.batches.stream()
                .filter(b -> b.getBatchNumber().equals(batchNumber))
                .findFirst()
                .orElse(null);

        if (targetBatch == null) {
            targetBatch = new InventoryBatch(batchNumber, expiryDate, 0, this);
            this.batches.add(targetBatch);
        }

        targetBatch.addStock(amount);
        this.stockQuantity += amount;
    }

    public void addStock(int amount) {
        addStock(amount, "DEFAULT_BATCH", LocalDate.now().plusYears(5));
    }

    public void reduceStock(int amount) {
        if (amount <= 0) {
            throw InvalidProductOperationException.invalidStockAmount();
        }
        if (this.stockQuantity < amount) {
            throw InvalidProductOperationException.insufficientStock();
        }

        int remaining = amount;
        List<InventoryBatch> sortedBatches = this.batches.stream()
                .filter(b -> b.getQuantity() > 0)
                .sorted(Comparator.comparing(InventoryBatch::getExpiryDate))
                .collect(Collectors.toList());

        for (InventoryBatch batch : sortedBatches) {
            if (remaining <= 0) {
                break;
            }
            int toDeduct = Math.min(batch.getQuantity(), remaining);
            batch.reduceStock(toDeduct);
            remaining -= toDeduct;
        }

        this.stockQuantity -= amount;
    }

    public void reduceStockFromBatch(int amount, String batchNumber) {
        if (amount <= 0) {
            throw InvalidProductOperationException.invalidStockAmount();
        }

        InventoryBatch targetBatch = this.batches.stream()
                .filter(b -> b.getBatchNumber().equals(batchNumber))
                .findFirst()
                .orElseThrow(() -> InvalidProductOperationException.batchNotFound(batchNumber));

        if (targetBatch.getQuantity() < amount) {
            throw InvalidProductOperationException.insufficientBatchStock(batchNumber, targetBatch.getQuantity(), amount);
        }

        targetBatch.reduceStock(amount);
        this.stockQuantity -= amount;
    }

    public Integer getId() {
        return id;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public ProductVariant getVariant() {
        return variant;
    }

    public List<InventoryBatch> getBatches() {
        return Collections.unmodifiableList(batches);
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Inventory i && id != null && id.equals(i.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}