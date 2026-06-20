package com.example.new_toy_store.cart.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import jakarta.persistence.*;
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
public class Cart extends BaseAuditEntity {

    public static final int MAX_CART_ITEMS = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Integer userId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    protected Cart() {}

    public Cart(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("ID người dùng không được để trống");
        }
        this.userId = userId;
    }

    public void addItem(Integer productId, Integer variantId, int quantity) {
        Optional<CartItem> existingItem = items.stream()
                .filter(item -> item.getProductId().equals(productId) && item.getVariantId().equals(variantId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().addQuantity(quantity);
        } else {
            if (this.items.size() >= MAX_CART_ITEMS) {
                throw new IllegalStateException("Giỏ hàng đã đạt giới hạn tối đa " + MAX_CART_ITEMS + " loại mặt hàng khác nhau. Vui lòng thanh toán bớt sản phẩm trước khi thêm mới.");
            }

            CartItem newItem = new CartItem(productId, variantId, quantity);
            newItem.setCart(this);
            items.add(newItem);
        }
    }

    public void updateItemQuantity(Integer itemId, int newQuantity) {
        CartItem item = items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm trong giỏ hàng"));
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

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Cart c && id != null && id.equals(c.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}