package com.example.new_toy_store.imports.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
            throw new IllegalStateException("Không thể thêm sản phẩm vào phiếu nhập đã chốt hoặc đã hủy.");
        }
        ImportNoteItem item = new ImportNoteItem(productId, variantId, productName, quantity, importPrice);
        item.setImportNote(this);
        this.items.add(item);
        this.totalAmount += item.getTotalPrice();
    }

    public void complete() {
        if (!this.status.canComplete()) {
            throw new IllegalStateException("Không thể hoàn thành phiếu nhập ở trạng thái này");
        }
        this.status = ImportStatus.COMPLETED;
    }

    public void cancel() {
        if (!this.status.canCancel()) {
            throw new IllegalStateException("Không thể hủy phiếu nhập ở trạng thái này");
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