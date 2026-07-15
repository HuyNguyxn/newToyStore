package com.example.new_toy_store.supplier_return.domain;

import com.example.new_toy_store.global.common.BaseSoftDeleteEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "supplier_return_images")
public class SupplierReturnImage extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_return_id", nullable = false)
    private SupplierReturn supplierReturn;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    protected SupplierReturnImage() {
    }

    public SupplierReturnImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    void assignToReturn(SupplierReturn supplierReturn) {
        this.supplierReturn = supplierReturn;
    }

    public Integer getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}