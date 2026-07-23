package com.example.new_toy_store.product.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer>, JpaSpecificationExecutor<ProductVariant> {

    @EntityGraph(attributePaths = {"inventory", "attributes"})
    List<ProductVariant> findByProductId(Integer productId);

    @EntityGraph(attributePaths = {"product", "inventory", "attributes"})
    Optional<ProductVariant> findById(Integer id);

    @EntityGraph(attributePaths = {"product", "inventory", "attributes"})
    @Query("SELECT v FROM ProductVariant v WHERE v.id = :id")
    Optional<ProductVariant> findByIdWithProduct(@Param("id") Integer id);
}
