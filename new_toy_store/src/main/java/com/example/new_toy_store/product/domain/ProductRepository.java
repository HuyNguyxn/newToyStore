package com.example.new_toy_store.product.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.variants WHERE p.id = :id")
    Product findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.variants WHERE p.id IN :ids")
    List<Product> findAllByIdsWithDetails(@Param("ids") Set<Integer> ids);

    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);

    @Query("SELECT DISTINCT p.id FROM Product p JOIN p.variants v WHERE v.id IN :variantIds")
    Set<Integer> findProductIdsByVariantIds(@Param("variantIds") Set<Integer> variantIds);
}