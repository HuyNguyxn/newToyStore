package com.example.new_toy_store.product.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Set;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Override
    Page<Product> findAll(Pageable pageable);

    Page<Product> findByCategoriesId(Integer categoryId, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseAndStatus(String keyword, ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.variants WHERE p.id = :id")
    Product findByIdWithDetails(@Param("id") Integer id);

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.images LEFT JOIN FETCH p.variants WHERE p.id IN :ids")
    List<Product> findAllByIdsWithDetails(@Param("ids") Set<Integer> ids);

    @Query("SELECT DISTINCT p.id FROM Product p JOIN p.variants v WHERE v.id IN :variantIds")
    Set<Integer> findProductIdsByVariantIds(@Param("variantIds") Set<Integer> variantIds);

    Page<Product> findByBasePriceBetweenAndStatus(double minPrice, double maxPrice, ProductStatus status, Pageable pageable);

    Page<Product> findByCategoriesIdAndBasePriceBetweenAndStatus(Integer categoryId, double minPrice, double maxPrice, ProductStatus status, Pageable pageable);
}