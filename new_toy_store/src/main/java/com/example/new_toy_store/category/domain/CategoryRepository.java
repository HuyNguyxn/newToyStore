package com.example.new_toy_store.category.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsBySlug(String slug);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    List<Category> findAllRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.status = 'VISIBLE'")
    List<Category> findVisibleRootCategories();
}