package com.example.new_toy_store.review.domain;

import com.example.new_toy_store.order.domain.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    Optional<Review> findByUserIdAndProductId(Integer userId, Integer productId);

    Page<Review> findByUserId(Integer userId, Pageable pageable);

    @Query("SELECT r FROM Review r WHERE r.productId = :productId AND r.status = 'PUBLISHED' AND (:rating IS NULL OR r.rating = :rating)")
    Page<Review> findPublicReviewsWithFilter(@Param("productId") Integer productId, @Param("rating") Integer rating, Pageable pageable);

    Page<Review> findByProductId(Integer productId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.status = 'PUBLISHED'")
    Double calculateAverageRating(@Param("productId") Integer productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'PUBLISHED'")
    Integer countPublishedReviews(@Param("productId") Integer productId);

    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'PUBLISHED' GROUP BY r.rating")
    List<Object[]> countReviewsByRatingGroup(@Param("productId") Integer productId);

    Optional<Review> findByOrderItemId(Integer orderItemId);

    Optional<OrderItem> findCompletedOrderItem(@Param("orderItemId") Integer orderItemId, @Param("userId") Integer userId);
}