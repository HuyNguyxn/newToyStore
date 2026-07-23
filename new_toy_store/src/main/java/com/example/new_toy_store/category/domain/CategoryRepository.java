package com.example.new_toy_store.category.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer>, JpaSpecificationExecutor<Category> {

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"parent", "subCategories"})
    List<Category> findByParentIsNullOrderByDisplayOrderAsc();

    @Override
    @EntityGraph(attributePaths = {"parent", "subCategories"})
    Optional<Category> findById(Integer id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Category c
               SET c.status = :status,
                   c.version = c.version + 1,
                   c.updatedAt = CURRENT_TIMESTAMP
             WHERE c.id = :id
               AND c.version = :version
            """)
    int updateStatusWithVersion(
            @Param("id") Integer id,
            @Param("status") CategoryStatus status,
            @Param("version") Long version
    );
}
