package com.example.new_toy_store.supplier.domain;

import com.example.new_toy_store.global.common.BaseRootEntity;
import com.example.new_toy_store.supplier.domain.exception.InvalidSupplierOperationException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "suppliers",
        indexes = {
                @Index(name = "idx_supplier_phone", columnList = "phone_number", unique = true),
                @Index(name = "idx_supplier_name", columnList = "name"),
                @Index(name = "idx_supplier_status", columnList = "status")
        }
)
public class Supplier extends BaseRootEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(length = 150)
    private String email;

    @Column(length = 500)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SupplierStatus status = SupplierStatus.ACTIVE;

    protected Supplier() {}

    public Supplier(String name, String phoneNumber, String email, String address) {
        if (name == null || name.trim().isEmpty()) throw InvalidSupplierOperationException.emptyField("Tên nhà cung cấp");
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) throw InvalidSupplierOperationException.emptyField("Số điện thoại");
        this.name = name; this.phoneNumber = phoneNumber; this.email = email; this.address = address;
    }

    public void updateInfo(String name, String phoneNumber, String email, String address) {
        if (name != null && !name.trim().isEmpty()) this.name = name;
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) this.phoneNumber = phoneNumber;
        this.email = email; this.address = address;
    }

    public void setStatus(SupplierStatus status) { if (status != null) this.status = status; }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public SupplierStatus getStatus() { return status; }

    @Override public boolean equals(Object o) { return this == o || (o instanceof Supplier u && id != null && id.equals(u.id)); }
    @Override public int hashCode() { return getClass().hashCode(); }
}