package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.customer_return.domain.CustomerReturn;
import com.example.new_toy_store.customer_return.domain.CustomerReturnStatus;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class CustomerReturnSpecification {
    public static Specification<CustomerReturn> filter(String status, Integer orderId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null && !status.trim().isEmpty()) { predicates.add(cb.equal(root.get("status"), CustomerReturnStatus.from(status))); }
            if (orderId != null) { predicates.add(cb.equal(root.get("orderId"), orderId)); }
            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
