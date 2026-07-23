package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.user.application.dto.request.UserFilterRequest;
import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.UserRole;
import com.example.new_toy_store.user.domain.UserStatus;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    public static Specification<User> filter(UserFilterRequest request) {
        if (request == null) return Specification.where(null);

        Specification<User> spec = Specification.where(hasKeyword(request.getKeyword()));

        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            spec = spec.and(hasRole(UserRole.from(request.getRole())));
        }

        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            spec = spec.and(hasStatus(UserStatus.from(request.getStatus())));
        }

        return spec;
    }

    private static Specification<User> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) return null;
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("phoneNumber")), pattern)
            );
        };
    }

    private static Specification<User> hasRole(UserRole role) {
        return (root, query, cb) -> role == null ? null : cb.equal(root.get("role"), role);
    }

    private static Specification<User> hasStatus(UserStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }
}
