package com.example.new_toy_store.product.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

    @EntityGraph(attributePaths = {"variants", "variants.inventory"})
    Optional<Product> findById(Integer id);

    @EntityGraph(attributePaths = {"images", "variants"})
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Product findByIdWithDetails(@Param("id") Integer id);

    @EntityGraph(attributePaths = {"variants", "variants.inventory"})
    @Query("SELECT p FROM Product p WHERE p.id IN :ids")
    List<Product> findAllByIdsWithDetails(@Param("ids") Set<Integer> ids);

    @Query("SELECT DISTINCT p.id FROM Product p JOIN p.variants v WHERE v.id IN :variantIds")
    Set<Integer> findProductIdsByVariantIds(@Param("variantIds") Set<Integer> variantIds);
}
