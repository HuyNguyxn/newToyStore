package com.example.new_toy_store.imports.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "import_note_items")
public class ImportNoteItem extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "import_note_id", nullable = false)
    private ImportNote importNote;

    @Column(nullable = false)
    private Integer productId;

    @Column(nullable = false)
    private Integer variantId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private double importPrice;

    protected ImportNoteItem() {}

    public ImportNoteItem(Integer productId, Integer variantId, String productName, int quantity, double importPrice) {
        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;
        this.quantity = quantity;
        this.importPrice = importPrice;
    }

    void setImportNote(ImportNote importNote) { this.importNote = importNote; }
    public double getTotalPrice() { return this.quantity * this.importPrice; }

    public Integer getId() { return id; }
    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getImportPrice() { return importPrice; }
}