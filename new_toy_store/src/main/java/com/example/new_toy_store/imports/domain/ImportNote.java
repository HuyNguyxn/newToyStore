package com.example.new_toy_store.imports.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import com.example.new_toy_store.imports.domain.exception.InvalidImportOperationException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "import_notes",
        indexes = {
                @Index(name = "idx_import_note_supplier_id", columnList = "supplier_id"),
                @Index(name = "idx_import_note_status", columnList = "status")
        }
)
public class ImportNote extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;


    @Column(name = "supplier_id", nullable = false)
    private Integer supplierId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ImportStatus status = ImportStatus.PENDING;

    @Column(nullable = false)
    private double totalAmount = 0.0;

    @Column(length = 500)
    private String note;

    @OneToMany(mappedBy = "importNote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImportNoteItem> items = new ArrayList<>();

    protected ImportNote() {}

    public ImportNote(Integer supplierId, String note) {
        if (supplierId == null) {
            throw new IllegalArgumentException("Mã nhà cung cấp không được để trống");
        }
        this.supplierId = supplierId;
        this.note = note;
    }

    public void addItem(Integer productId, Integer variantId, String productName, int quantity, double importPrice) {
        if (!this.status.canModifyItems()) {
            throw InvalidImportOperationException.invalidStatusTransition("thêm sản phẩm vào");
        }

        Optional<ImportNoteItem> existingItem = items.stream()
                .filter(item -> item.getVariantId().equals(variantId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().addQuantity(quantity);
        } else {
            ImportNoteItem item = new ImportNoteItem(productId, variantId, productName, quantity, importPrice);
            item.setImportNote(this);
            this.items.add(item);
        }

        double additionalAmount = quantity * importPrice;
        this.totalAmount = Math.max(0.0, Math.round((this.totalAmount + additionalAmount) * 100.0) / 100.0);
    }

    public void complete() {
        if (!this.status.canComplete()) {
            throw InvalidImportOperationException.invalidStatusTransition("hoàn thành");
        }
        if (this.items.isEmpty()) {
            throw InvalidImportOperationException.emptyItems();
        }
        this.status = ImportStatus.COMPLETED;
    }

    public void cancel() {
        if (!this.status.canCancel()) {
            throw InvalidImportOperationException.invalidStatusTransition("hủy");
        }
        this.status = ImportStatus.CANCELLED;
    }

    public Integer getId() { return id; }   
    public Integer getSupplierId() { return supplierId; }
    public ImportStatus getStatus() { return status; }
    public double getTotalAmount() { return totalAmount; }
    public String getNote() { return note; }
    public List<ImportNoteItem> getItems() { return Collections.unmodifiableList(items); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof ImportNote u && id != null && id.equals(u.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}