package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.moderation.application.dto.request.BlacklistedWordFilterRequest;
import com.example.new_toy_store.moderation.domain.BlacklistedWord;
import org.springframework.data.jpa.domain.Specification;

public class BlacklistedWordSpecification {

    public static Specification<BlacklistedWord> filter(BlacklistedWordFilterRequest request) {
        if (request == null) return Specification.where(null);

        return Specification.where(BaseSpecification.contains("word", request.getKeyword()));
    }
}