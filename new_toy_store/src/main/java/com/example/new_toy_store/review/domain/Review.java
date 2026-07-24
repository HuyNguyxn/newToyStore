package com.example.new_toy_store.review.domain;

import com.example.new_toy_store.global.common.BaseRootEntity;
import com.example.new_toy_store.review.domain.exception.InvalidReviewOperationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "reviews",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_review_order_item", columnNames = {"order_item_id"})
        },
        indexes = {
                @Index(name = "idx_review_product_id", columnList = "product_id"),
                @Index(name = "idx_review_status", columnList = "status"),
                @Index(name = "idx_review_user_id", columnList = "user_id")
        }
)
public class Review extends BaseRootEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "order_item_id", nullable = false)
    private Integer orderItemId;

    @Column(name = "variant_attributes_snapshot", nullable = false)
    private String variantAttributesSnapshot;

    @Column(nullable = false)
    private int rating;

    @Column(length = 1000)
    private String comment;

    @Column(name = "admin_reply", length = 1000)
    private String adminReply;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status = ReviewStatus.PUBLISHED;

    protected Review() {
    }

    public Review(Integer userId,
                  Integer productId,
                  Integer orderItemId,
                  String variantAttributesSnapshot,
                  int rating,
                  String comment) {
        if (userId == null) {
            throw InvalidReviewOperationException.missingRequirement("userId");
        }
        if (productId == null) {
            throw InvalidReviewOperationException.missingRequirement("productId");
        }
        if (orderItemId == null) {
            throw InvalidReviewOperationException.missingRequirement("orderItemId");
        }
        if (rating < 1 || rating > 5) {
            throw InvalidReviewOperationException.invalidRating(rating);
        }

        this.userId = userId;
        this.productId = productId;
        this.orderItemId = orderItemId;
        this.variantAttributesSnapshot = variantAttributesSnapshot != null ? variantAttributesSnapshot : "Mặc định";
        this.rating = rating;
        this.comment = comment;
    }

    public void updateByUser(int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw InvalidReviewOperationException.invalidRating(rating);
        }
        this.rating = rating;
        this.comment = comment;
        this.adminReply = null;
        this.status = ReviewStatus.PUBLISHED;
    }

    public void replyByAdmin(String reply) {
        if (reply == null || reply.trim().isEmpty()) {
            throw InvalidReviewOperationException.emptyReply();
        }
        this.adminReply = reply;
    }

    public void changeStatus(ReviewStatus newStatus) {
        if (newStatus == null || this.status == newStatus) {
            return;
        }
        if (!this.status.canTransitionTo(newStatus)) {
            throw InvalidReviewOperationException.invalidStatusTransition(this.status.getCode(), newStatus.getCode());
        }
        this.status = newStatus;
    }

    public Integer getId() {
        return id;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getProductId() {
        return productId;
    }

    public Integer getOrderItemId() {
        return orderItemId;
    }

    public String getVariantAttributesSnapshot() {
        return variantAttributesSnapshot;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof Review review && id != null && id.equals(review.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
