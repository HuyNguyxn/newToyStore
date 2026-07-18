package com.example.new_toy_store.global.specification;

import org.springframework.data.jpa.domain.Specification;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class BaseSpecification {

    public static <T> Specification<T> contains(String field, String value) {
        return (root, query, cb) -> {
            if (value == null || value.trim().isEmpty()) return null;
            return cb.like(cb.lower(root.get(field)), "%" + value.trim().toLowerCase() + "%");
        };
    }

    public static <T> Specification<T> isEqual(String field, Object value) {
        return (root, query, cb) -> {
            if (value == null) return null;
            if (value instanceof String str && str.trim().isEmpty()) return null;
            return cb.equal(root.get(field), value);
        };
    }

    public static <T> Specification<T> dateBetween(String field, LocalDate startDate, LocalDate endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) return null;
            if (startDate != null && endDate != null) {
                return cb.between(root.get(field), startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
            } else if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get(field), startDate.atStartOfDay());
            } else {
                return cb.lessThanOrEqualTo(root.get(field), endDate.atTime(LocalTime.MAX));
            }
        };
    }

    public static <T> Specification<T> isDistinct() {
        return (root, query, cb) -> {
            query.distinct(true);
            return null;
        };
    }

    public static <T> Specification<T> dateBetween(String field, LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, cb) -> {
            if (startDate == null && endDate == null) return null;
            if (startDate != null && endDate != null) {
                return cb.between(root.get(field), startDate, endDate);
            } else if (startDate != null) {
                return cb.greaterThanOrEqualTo(root.get(field), startDate);
            } else {
                return cb.lessThanOrEqualTo(root.get(field), endDate);
            }
        };
    }
}