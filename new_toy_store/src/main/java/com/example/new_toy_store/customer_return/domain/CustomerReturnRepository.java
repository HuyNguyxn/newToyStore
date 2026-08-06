package com.example.new_toy_store.customer_return.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerReturnRepository extends JpaRepository<CustomerReturn, Integer>, JpaSpecificationExecutor<CustomerReturn> {

    @Query("SELECT COUNT(c) > 0 FROM CustomerReturn c WHERE c.orderId = :orderId AND c.status NOT IN ('REJECTED', 'CANCELLED', 'REFUNDED', 'REPLACED')")
    boolean hasActiveReturnRequest(@Param("orderId") Integer orderId);

    @EntityGraph(attributePaths = {"items"})
    Page<CustomerReturn> findAll(Specification<CustomerReturn> spec, Pageable pageable);

    long countByStatus(CustomerReturnStatus status);

    @EntityGraph(attributePaths = {"items"})
    Optional<CustomerReturn> findById(Integer id);

    @Query("SELECT c FROM CustomerReturn c WHERE c.status = 'NEEDS_MORE_INFO' AND c.deadlineForExtraInfo < :now")
    List<CustomerReturn> findExpiredRequests(@Param("now") LocalDateTime now);
}