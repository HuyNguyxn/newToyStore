package com.example.new_toy_store.supplier_return.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SupplierReturnRepository extends JpaRepository<SupplierReturn, Integer>, JpaSpecificationExecutor<SupplierReturn> {

    Page<SupplierReturn> findAll(Specification<SupplierReturn> spec, Pageable pageable);

    Optional<SupplierReturn> findById(Integer id);

    boolean existsByImportNoteIdAndStatusNotIn(Integer importNoteId, Collection<SupplierReturnStatus> statuses);

    long countByStatus(SupplierReturnStatus status);

    List<SupplierReturn> findAllByStatusAndUpdatedAtBefore(
            SupplierReturnStatus status,
            LocalDateTime cutoffTime
    );

    List<SupplierReturn> findAllByStatusAndUpdatedAtBetween(
            SupplierReturnStatus status,
            LocalDateTime startCutoffTime,
            LocalDateTime endCutoffTime
    );
}
