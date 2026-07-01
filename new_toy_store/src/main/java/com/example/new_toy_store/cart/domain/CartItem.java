package com.example.new_toy_store.cart.domain;

import com.example.new_toy_store.cart.domain.exception.InvalidCartOperationException;
import com.example.new_toy_store.global.common.BaseAuditEntity;
import jakarta.persistence.*;

@Entity
@Table(
        name = "cart_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cart_variant", columnNames = {"cart_id", "variant_id"})
        },
        indexes = {
                @Index(name = "idx_cart_item_cart", columnList = "cart_id"),
                @Index(name = "idx_cart_item_product", columnList = "product_id"),
                @Index(name = "idx_cart_item_variant", columnList = "variant_id")
        }
)
public class CartItem extends BaseAuditEntity {

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

    protected CartItem() {}

    public CartItem(Integer productId, Integer variantId, int quantity) {
        if (productId == null || variantId == null) {
            throw InvalidCartOperationException.nullProductOrVariant();
        }
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
    }

    void setCart(Cart cart) {
        this.cart = cart;
    }

    public void addQuantity(int amount) {
        if (amount <= 0) {
            throw InvalidCartOperationException.invalidQuantity(amount);
        }
        this.quantity += amount;
    }

    public void updateQuantity(int newQuantity) {
        if (newQuantity <= 0) {
            throw InvalidCartOperationException.invalidQuantity(newQuantity);
        }
        this.quantity = newQuantity;
    }

    public Integer getId() { return id; }
    public Cart getCart() { return cart; }
    public Integer getProductId() { return productId; }
    public Integer getVariantId() { return variantId; }
    public int getQuantity() { return quantity; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof CartItem c && id != null && id.equals(c.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}