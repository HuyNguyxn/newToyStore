package com.example.new_toy_store.cart.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "cart_items",
        indexes = {
                @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
                @Index(name = "idx_cart_item_product", columnList = "product_id")
        }
)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "variant_id", nullable = false)
    private Integer variantId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    protected CartItem() {}

    public CartItem(Integer productId, Integer variantId, int quantity) {
        if (productId == null || variantId == null) {
            throw new IllegalArgumentException("Product and Variant IDs are required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
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

    void setCart(Cart cart) {
        this.cart = cart;
    }

    public void addQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        this.quantity += amount;
    }

    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        this.quantity = newQuantity;
    }

    public Integer getId() { return id; }
    public Cart getCart() { return cart; }
    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof CartItem c && id != null && id.equals(c.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}