package com.example.new_toy_store.imports.domain;

import com.example.new_toy_store.global.common.BaseTimeEntity;
import com.example.new_toy_store.imports.domain.exception.InvalidImportOperationException;
import jakarta.persistence.*;

@Entity
@Table(
        name = "import_note_items",
        uniqueConstraints = {@UniqueConstraint(name = "uk_import_note_variant", columnNames = {"import_note_id", "variant_id"})},
        indexes = {
                @Index(name = "idx_import_item_note_id", columnList = "import_note_id"),
                @Index(name = "idx_import_item_variant_id", columnList = "variant_id")
        }
)
public class ImportNoteItem extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_note_id", nullable = false)
    private ImportNote importNote;

    @Column(nullable = false)
    private Integer productId;

    @Column(nullable = false)
    private Integer variantId;

    @Column(nullable = false, length = 255)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double importPrice;

    protected ImportNoteItem() {}

    public ImportNoteItem(Integer productId, Integer variantId, String productName, int quantity, double importPrice) {
        if (productId == null || variantId == null) throw InvalidImportOperationException.missingItemIds();
        if (quantity <= 0) throw InvalidImportOperationException.invalidQuantity();
        if (importPrice < 0) throw InvalidImportOperationException.negativePrice();
        this.productId = productId; this.variantId = variantId; this.productName = productName; this.quantity = quantity; this.importPrice = importPrice;
    }

    void setImportNote(ImportNote importNote) { this.importNote = importNote; }

    public void addQuantity(int amount) {
        if (amount <= 0) throw InvalidImportOperationException.invalidQuantity();
        this.quantity += amount;
    }

    public double getTotalPrice() { return Math.max(0.0, Math.round((this.quantity * this.importPrice) * 100.0) / 100.0); }

    public Integer getId() { return id; }
    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getImportPrice() { return importPrice; }

    @Override public boolean equals(Object o) { return this == o || (o instanceof ImportNoteItem u && id != null && id.equals(u.id)); }
    @Override public int hashCode() { return getClass().hashCode(); }
}