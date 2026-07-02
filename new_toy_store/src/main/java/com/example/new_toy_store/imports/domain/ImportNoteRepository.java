package com.example.new_toy_store.imports.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImportNoteRepository extends JpaRepository<ImportNote, Integer> {

    @Query("SELECT i FROM ImportNote i LEFT JOIN FETCH i.items WHERE i.id = :id")
    ImportNote findByIdWithItems(@Param("id") Integer id);

    @Query("SELECT i FROM ImportNote i WHERE " +
            "(:supplierId IS NULL OR i.supplierId = :supplierId) AND " +
            "(:status IS NULL OR i.status = :status)")
    Page<ImportNote> searchImports(@Param("supplierId") Integer supplierId,
                                   @Param("status") ImportStatus status,
                                   Pageable pageable);
}