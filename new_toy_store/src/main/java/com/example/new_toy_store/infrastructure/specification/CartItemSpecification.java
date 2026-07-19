package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.cart.domain.CartItem;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class CartItemSpecification {

    public static Specification<CartItem> isUpdatedBetween(LocalDateTime start, LocalDateTime end) {
        return (root, query, cb) -> cb.between(root.get("updatedAt"), start, end);
    }

    public static Specification<CartItem> isExpired(LocalDateTime threshold) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("updatedAt"), threshold);
    }

    public static Specification<CartItem> isSelected(boolean selected) {
        return (root, query, cb) -> cb.equal(root.get("selected"), selected);
    }
}