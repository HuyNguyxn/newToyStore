package com.example.new_toy_store.category.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer>, JpaSpecificationExecutor<Category> {

    boolean existsBySlug(String slug);

    @EntityGraph(attributePaths = {"parent", "children"})
    List<Category> findByParentIsNullOrderByDisplayOrderAsc();

    @EntityGraph(attributePaths = {"parent", "children"})
    Optional<Category> findById(Integer id);
}