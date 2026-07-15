package com.example.new_toy_store.supplier_return.domain;

import com.example.new_toy_store.global.common.BaseSoftDeleteEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "supplier_return_histories")
public class SupplierReturnHistory extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_return_id", nullable = false)
    private SupplierReturn supplierReturn;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status")
    private SupplierReturnStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false)
    private SupplierReturnStatus newStatus;

    @Column(name = "action_by", nullable = false)
    private String actionBy;

    @Column(length = 500)
    private String note;

    protected SupplierReturnHistory() {
    }

    public SupplierReturnHistory(
            SupplierReturnStatus oldStatus,
            SupplierReturnStatus newStatus,
            String actionBy,
            String note) {

        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.actionBy = actionBy;
        this.note = note;
    }

    void assignToReturn(SupplierReturn supplierReturn) {
        this.supplierReturn = supplierReturn;
    }

    public Integer getId() {
        return id;
    }

    public SupplierReturnStatus getOldStatus() {
        return oldStatus;
    }

    public SupplierReturnStatus getNewStatus() {
        return newStatus;
    }

    public String getActionBy() {
        return actionBy;
    }

    public String getNote() {
        return note;
    }
}