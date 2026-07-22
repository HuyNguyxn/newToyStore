package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.cart.domain.CartItem;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public final class CartItemSpecification {

    private CartItemSpecification() {
    }

    public static Specification<CartItem> isUpdatedBetween(LocalDateTime start, LocalDateTime end) {
        return Specification.allOf(updatedAtOnOrAfter(start), updatedAtBefore(end));
    }

    public static Specification<CartItem> isExpired(LocalDateTime threshold) {
        return (root, query, cb) -> threshold == null
                ? cb.conjunction()
                : cb.lessThan(root.get("updatedAt"), threshold);
    }

    public static Specification<CartItem> isSelected(boolean selected) {
        return (root, query, cb) -> cb.equal(root.get("isSelected"), selected);
    }

    private static Specification<CartItem> updatedAtOnOrAfter(LocalDateTime start) {
        return (root, query, cb) -> start == null
                ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("updatedAt"), start);
    }

    private static Specification<CartItem> updatedAtBefore(LocalDateTime end) {
        return (root, query, cb) -> end == null
                ? cb.conjunction()
                : cb.lessThan(root.get("updatedAt"), end);
    }
}
