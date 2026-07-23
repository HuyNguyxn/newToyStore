package com.example.new_toy_store.user.domain;

import com.example.new_toy_store.global.common.BaseSoftDeleteEntity;
import com.example.new_toy_store.user.domain.exception.InvalidUserOperationException;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "addresses",
        indexes = {@Index(name = "idx_address_user_id", columnList = "user_id")}
)
public class Address extends BaseSoftDeleteEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @BatchSize(size = 100)
    private User user;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false)
    private String receiverPhone;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    protected Address() {}

    public Address(String receiverName, String receiverPhone, String detailAddress, boolean isDefault) {
        if (receiverName == null || receiverName.trim().isEmpty()) {
            throw InvalidUserOperationException.inputDataInvalid("receiverName", "Tên người nhận không được để trống");
        }
        if (receiverPhone == null || receiverPhone.trim().isEmpty()) {
            throw InvalidUserOperationException.inputDataInvalid("receiverPhone", "Số điện thoại người nhận không được để trống");
        }
        if (detailAddress == null || detailAddress.trim().isEmpty()) {
            throw InvalidUserOperationException.inputDataInvalid("detailAddress", "Địa chỉ chi tiết không được để trống");
        }
        this.receiverName = receiverName; this.receiverPhone = receiverPhone; this.detailAddress = detailAddress; this.isDefault = isDefault;
    }

    void setUser(User user) { this.user = user; }

    public void updateInfo(String receiverName, String receiverPhone, String detailAddress) {
        if (receiverName != null && !receiverName.trim().isEmpty()) this.receiverName = receiverName;
        if (receiverPhone != null && !receiverPhone.trim().isEmpty()) this.receiverPhone = receiverPhone;
        if (detailAddress != null && !detailAddress.trim().isEmpty()) this.detailAddress = detailAddress;
    }

    void makeDefault() { this.isDefault = true; }
    void removeDefault() { this.isDefault = false; }

    public Integer getId() { return id; }
    public User getUser() { return user; }
    public String getReceiverName() { return receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public String getDetailAddress() { return detailAddress; }
    public boolean isDefault() { return isDefault; }

    @Override public boolean equals(Object o) { return this == o || (o instanceof Address a && id != null && id.equals(a.id)); }
    @Override public int hashCode() { return getClass().hashCode(); }
}
