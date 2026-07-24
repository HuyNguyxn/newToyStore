package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.promotion.domain.Promotion;
import com.example.new_toy_store.promotion.domain.PromotionScope;
import org.springframework.data.jpa.domain.Specification;

import java.util.Locale;

public final class PromotionSpecification {

    private PromotionSpecification() {
    }

    public static Specification<Promotion> filter(PromotionScope scope, Boolean active, String keyword) {
        return Specification.where(hasScope(scope))
                .and(hasActive(active))
                .and(hasKeyword(keyword));
    }

    public static Specification<Promotion> hasScope(PromotionScope scope) {
        return BaseSpecification.isEqual("scope", scope);
    }

    public static Specification<Promotion> hasActive(Boolean active) {
        return BaseSpecification.isEqual("isActive", active);
    }

    public static Specification<Promotion> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("code")), pattern),
                    cb.like(cb.lower(root.get("name")), pattern)
            );
        };
    }
}
