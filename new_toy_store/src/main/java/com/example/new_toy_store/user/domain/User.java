package com.example.new_toy_store.user.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_status", columnList = "status")
        }
)
public class User extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    protected User() {}

    public User(String email, String password, String fullName, String phoneNumber, UserRole role) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.role = role != null ? role : UserRole.CUSTOMER;
        this.status = UserStatus.UNVERIFIED;
    }

    private void checkIfModificationIsAllowed() {
        if (this.status == null || !this.status.canModifyData()) {
            throw new IllegalStateException("Tài khoản đang bị khóa, không thể thực hiện thay đổi dữ liệu.");
        }
    }

    public void updateProfile(String fullName, String phoneNumber, String avatarUrl) {
        checkIfModificationIsAllowed();
        if (fullName != null && !fullName.trim().isEmpty()) {
            this.fullName = fullName;
        }
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            this.phoneNumber = phoneNumber;
        }
        if (avatarUrl != null && !avatarUrl.trim().isEmpty()) {
            this.avatarUrl = avatarUrl;
        }
    }

    public void updatePassword(String newPassword) {
        checkIfModificationIsAllowed();
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        this.password = newPassword;
    }

    public void activate() {
        if (this.status != UserStatus.UNVERIFIED) {
            throw new IllegalStateException("Chỉ tài khoản chưa xác thực mới có thể tiến hành kích hoạt.");
        }
        this.status = UserStatus.ACTIVE;
    }

    public void lockAccount() {
        this.status = UserStatus.LOCKED;
    }

    public void unlockAccount() {
        this.status = UserStatus.ACTIVE;
    }


    public void addAddress(Address address) {
        checkIfModificationIsAllowed();
        if (address != null) {
            if (address.isDefault() || this.addresses.isEmpty()) {
                clearDefaultAddresses();
                address.makeDefault();
            }
            address.setUser(this);
            this.addresses.add(address);
        }
    }

    public void removeAddress(Integer addressId) {
        checkIfModificationIsAllowed();
        this.addresses.removeIf(a -> a.getId() != null && a.getId().equals(addressId));
        ensureAtLeastOneDefaultAddress();
    }

    public void setDefaultAddress(Integer addressId) {
        checkIfModificationIsAllowed();
        boolean found = false;
        for (Address address : this.addresses) {
            if (address.getId() != null && address.getId().equals(addressId)) {
                address.makeDefault();
                found = true;
            } else {
                address.removeDefault();
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Address not found");
        }
    }

    private void clearDefaultAddresses() {
        for (Address address : this.addresses) {
            address.removeDefault();
        }
    }

    private void ensureAtLeastOneDefaultAddress() {
        if (!this.addresses.isEmpty()) {
            boolean hasDefault = this.addresses.stream().anyMatch(Address::isDefault);
            if (!hasDefault) {
                this.addresses.get(0).makeDefault();
            }
        }
    }

    @Override
    public void delete() {
        super.delete();
        this.email = this.email + "_deleted_" + System.currentTimeMillis();
        this.addresses.forEach(BaseAuditEntity::delete);
    }

    public Integer getId() { return id; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAvatarUrl() { return avatarUrl; }
    public UserRole getRole() { return role; }
    public UserStatus getStatus() { return status; }
    public List<Address> getAddresses() { return Collections.unmodifiableList(addresses); }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof User u && id != null && id.equals(u.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}