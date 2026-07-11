package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.moderation.application.dto.request.BlacklistedWordFilterRequest;
import com.example.new_toy_store.moderation.domain.BlacklistedWord;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

public class BlacklistedWordSpecification {

    public static Specification<BlacklistedWord> filter(BlacklistedWordFilterRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request != null && request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                String likePattern = "%" + request.getKeyword().trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("word")), likePattern));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}