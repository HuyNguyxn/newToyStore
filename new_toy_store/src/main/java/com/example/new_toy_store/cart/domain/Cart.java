package com.example.new_toy_store.cart.domain;

import com.example.new_toy_store.cart.domain.exception.CartItemNotFoundException;
import com.example.new_toy_store.cart.domain.exception.InvalidCartOperationException;
import com.example.new_toy_store.global.common.BaseRootEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Check(constraints = "user_id > 0 AND status IN ('ACTIVE', 'CHECKING_OUT')")
@Table(
        name = "carts",
        uniqueConstraints = @UniqueConstraint(name = "uk_cart_user", columnNames = "user_id")
)
public class Cart extends BaseRootEntity {

    public static final int MAX_CART_ITEMS = 50;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CartStatus status = CartStatus.ACTIVE;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {}

    public Cart(Integer userId) {
        if (userId == null) throw InvalidCartOperationException.nullUserId();
        if (userId <= 0) throw InvalidCartOperationException.invalidUserId(userId);
        this.userId = userId;
    }

    public void changeStatus(CartStatus newStatus) {
        if (!this.status.canTransitionTo(newStatus)) {
            throw InvalidCartOperationException.invalidStatusTransition(
                    this.status.name(),
                    newStatus == null ? "null" : newStatus.name()
            );
        }
        this.status = newStatus;
    }

    public void addItem(Integer productId, Integer variantId, int quantity, double addedPrice) {
        checkIfCartIsActive();
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getVariantId() != null && item.getVariantId().equals(variantId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().addQuantity(quantity);
        } else {
            if (this.items.size() >= MAX_CART_ITEMS) throw InvalidCartOperationException.maxItemsExceeded(MAX_CART_ITEMS);
            CartItem newItem = new CartItem(productId, variantId, quantity, addedPrice);
            newItem.setCart(this);
            items.add(newItem);
        }
    }

    public void updateItemQuantity(Integer itemId, int newQuantity) {
        checkIfCartIsActive();
        CartItem item = items.stream().filter(i -> i.getId().equals(itemId)).findFirst().orElseThrow(() -> CartItemNotFoundException.byItemId(itemId));
        item.updateQuantity(newQuantity);
    }

    public void toggleItemSelection(Integer itemId, boolean isSelected) {
        checkIfCartIsActive();
        CartItem item = items.stream().filter(i -> i.getId().equals(itemId)).findFirst().orElseThrow(() -> CartItemNotFoundException.byItemId(itemId));
        item.toggleSelection(isSelected);
    }

    public void removeItem(Integer itemId) {
        checkIfCartIsActive();
        boolean removed = items.removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw CartItemNotFoundException.byItemId(itemId);
        }
    }

    public void clearCart() {
        checkIfCartIsActive();
        items.clear();
    }

    public void completeCheckout() {
        if (status != CartStatus.CHECKING_OUT) {
            throw InvalidCartOperationException.checkoutNotInProgress();
        }
        items.removeIf(CartItem::isSelected);
        changeStatus(CartStatus.ACTIVE);
    }

    private void checkIfCartIsActive() {
        if (this.status != CartStatus.ACTIVE) {
            throw InvalidCartOperationException.cartNotActive();
        }
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public CartStatus getStatus() { return status; }
    public List<CartItem> getItems() { return Collections.unmodifiableList(items); }

    public double getFinalTotal() {
        return items.stream()
                .filter(CartItem::isSelected)
                .mapToDouble(item -> item.getQuantity() * item.getAddedPrice())
                .sum();
    }

    @Override public boolean equals(Object o) { return this == o || (o instanceof Cart c && id != null && id.equals(c.id)); }
    @Override public int hashCode() { return getClass().hashCode(); }
}
