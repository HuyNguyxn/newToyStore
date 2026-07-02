package com.example.new_toy_store.category.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    boolean existsBySlug(String slug);

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL")
    List<Category> findAllRootCategories();

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.status = 'VISIBLE'")
    List<Category> findVisibleRootCategories();

    @Query("SELECT c FROM Category c WHERE (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND (:status IS NULL OR c.status = :status)")
    Page<Category> searchCategories(@Param("keyword") String keyword, @Param("status") CategoryStatus status, Pageable pageable);
}