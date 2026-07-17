package com.example.new_toy_store.product.domain;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer>, JpaSpecificationExecutor<ProductVariant> {

    Optional<ProductVariant> findBySku(String sku);

    boolean existsBySku(String sku);

    List<ProductVariant> findByProductId(Integer productId);

    @EntityGraph(attributePaths = {"product"})
    Optional<ProductVariant> findById(Integer id);

    @Query("SELECT v FROM ProductVariant v JOIN FETCH v.product p WHERE v.id = :id")
    Optional<ProductVariant> findByIdWithProduct(@Param("id") Integer id);
}