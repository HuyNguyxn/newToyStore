package com.example.new_toy_store.review.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer>, JpaSpecificationExecutor<Review> {

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

    Page<Review> findByUserId(Integer userId, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Review r
               SET r.deletedAt = CURRENT_TIMESTAMP,
                   r.updatedAt = CURRENT_TIMESTAMP,
                   r.version = r.version + 1
             WHERE r.id = :id
               AND r.version = :version
            """)
    int softDeleteWithVersion(@Param("id") Integer id, @Param("version") Long version);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Review r
               SET r.status = :status,
                   r.updatedAt = CURRENT_TIMESTAMP,
                   r.version = r.version + 1
             WHERE r.id = :id
               AND r.version = :version
            """)
    int updateStatusWithVersion(@Param("id") Integer id,
                                @Param("version") Long version,
                                @Param("status") ReviewStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE Review r
               SET r.adminReply = :reply,
                   r.updatedAt = CURRENT_TIMESTAMP,
                   r.version = r.version + 1
             WHERE r.id = :id
               AND r.version = :version
            """)
    int updateAdminReplyWithVersion(@Param("id") Integer id,
                                    @Param("version") Long version,
                                    @Param("reply") String reply);
}
