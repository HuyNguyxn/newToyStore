package com.example.new_toy_store.user.domain;

import com.example.new_toy_store.global.common.BaseSoftDeleteEntity;
import jakarta.persistence.*;
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
        if (receiverName == null || receiverName.trim().isEmpty()) throw new IllegalArgumentException("Receiver name is required");
        if (receiverPhone == null || receiverPhone.trim().isEmpty()) throw new IllegalArgumentException("Receiver phone is required");
        if (detailAddress == null || detailAddress.trim().isEmpty()) throw new IllegalArgumentException("Detail address is required");
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