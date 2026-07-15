package com.example.new_toy_store.supplier_return.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Collection;
import java.util.Optional;

public interface SupplierReturnRepository extends JpaRepository<SupplierReturn, Integer>, JpaSpecificationExecutor<SupplierReturn> {

    @EntityGraph(attributePaths = {"items", "histories", "images"})
    Optional<SupplierReturn> findById(Integer id);

    boolean existsByImportNoteIdAndStatusNotIn(Integer importNoteId, Collection<SupplierReturnStatus> statuses);
}