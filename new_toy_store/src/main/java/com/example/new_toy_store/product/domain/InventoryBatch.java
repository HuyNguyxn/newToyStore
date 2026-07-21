package com.example.new_toy_store.product.domain;

import com.example.new_toy_store.global.common.BaseSoftDeleteEntity;
import com.example.new_toy_store.product.domain.exception.InvalidProductOperationException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "inventory_batches",
        indexes = {@Index(name = "idx_batch_inventory_id", columnList = "inventory_id")}
)
public class InventoryBatch extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    protected InventoryBatch() {
    }

    public InventoryBatch(String batchNumber, LocalDate expiryDate, int initialQuantity, Inventory inventory) {
        if (initialQuantity < 0) {
            throw InvalidProductOperationException.negativeInitialStock();
        }
        this.batchNumber = batchNumber;
        this.expiryDate = expiryDate;
        this.quantity = initialQuantity;
        this.inventory = inventory;
    }

    public void addStock(int amount) {
        if (amount <= 0) {
            throw InvalidProductOperationException.invalidStockAmount();
        }
        this.quantity += amount;
    }

    public void reduceStock(int amount) {
        if (amount <= 0) {
            throw InvalidProductOperationException.invalidStockAmount();
        }
        if (this.quantity < amount) {
            throw InvalidProductOperationException.insufficientStock();
        }
        this.quantity -= amount;
    }

    public Integer getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public int getQuantity() {
        return quantity;
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof InventoryBatch b && id != null && id.equals(b.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}