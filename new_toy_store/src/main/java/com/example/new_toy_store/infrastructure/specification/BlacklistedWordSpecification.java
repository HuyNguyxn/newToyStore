package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.moderation.application.dto.request.BlacklistedWordFilterRequest;
import com.example.new_toy_store.moderation.domain.BlacklistedWord;
import com.example.new_toy_store.moderation.domain.WordCategory;
import org.springframework.data.jpa.domain.Specification;

public final class BlacklistedWordSpecification {

    private BlacklistedWordSpecification() {
    }

    public static Specification<BlacklistedWord> filter(BlacklistedWordFilterRequest request) {
        if (request == null) {
            return Specification.where(null);
        }

        return Specification.where(hasKeyword(request.getKeyword()))
                .and(hasCategory(request.getCategory()));
    }

    public static Specification<BlacklistedWord> hasKeyword(String keyword) {
        return BaseSpecification.contains("word", keyword);
    }

    public static Specification<BlacklistedWord> hasCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            return Specification.where(null);
        }
        return hasCategory(WordCategory.from(category));
    }

    public static Specification<BlacklistedWord> hasCategory(WordCategory category) {
        return BaseSpecification.isEqual("category", category);
    }
}
