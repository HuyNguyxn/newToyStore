package com.example.new_toy_store.cart.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Entity
@Table(
        name = "carts",
        indexes = {
                @Index(name = "idx_cart_user_id", columnList = "user_id")
        }
)
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected Cart() {}

    public Cart(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        this.userId = userId;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addItem(Integer productId, Integer variantId, int quantity) {
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getProductId().equals(productId) && item.getVariantId().equals(variantId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().addQuantity(quantity);
        } else {
            CartItem newItem = new CartItem(productId, variantId, quantity);
            newItem.setCart(this);
            items.add(newItem);
        }
    }

    public void updateItemQuantity(Integer itemId, int newQuantity) {
        CartItem item = items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item not found in cart"));
        item.updateQuantity(newQuantity);
    }

    public void removeItem(Integer itemId) {
        items.removeIf(item -> item.getId().equals(itemId));
    }

    public void clearCart() {
        items.clear();
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Cart c && id != null && id.equals(c.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}