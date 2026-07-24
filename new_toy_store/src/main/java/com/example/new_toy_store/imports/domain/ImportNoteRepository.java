package com.example.new_toy_store.imports.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ImportNoteRepository extends JpaRepository<ImportNote, Integer>, JpaSpecificationExecutor<ImportNote> {

    @EntityGraph(attributePaths = "items")
    @Query("SELECT i FROM ImportNote i WHERE i.id = :id")
    Optional<ImportNote> findByIdWithItems(@Param("id") Integer id);

    Page<ImportNote> findAll(Specification<ImportNote> specification, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE ImportNote i
               SET i.status = :nextStatus,
                   i.updatedAt = CURRENT_TIMESTAMP,
                   i.version = i.version + 1
             WHERE i.id = :id
               AND i.version = :version
               AND i.status = :currentStatus
            """)
    int updateStatusWithVersion(@Param("id") Integer id,
                                @Param("version") Long version,
                                @Param("currentStatus") ImportStatus currentStatus,
                                @Param("nextStatus") ImportStatus nextStatus);
}
