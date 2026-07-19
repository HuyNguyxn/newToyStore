package com.example.new_toy_store.category.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer>, JpaSpecificationExecutor<Category> {

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"subCategories"})
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.displayOrder ASC")
    List<Category> findAllRootCategories();

    @EntityGraph(attributePaths = {"subCategories"})
    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.status = 'VISIBLE' ORDER BY c.displayOrder ASC")
    List<Category> findVisibleRootCategories();
}