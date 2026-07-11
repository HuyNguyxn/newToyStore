package com.example.new_toy_store.review.domain;

import com.example.new_toy_store.global.common.BaseAuditEntity;
import com.example.new_toy_store.promotion.domain.Promotion;
import jakarta.persistence.*;
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
public class Review extends BaseAuditEntity {

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

    protected Review() {}
    public Review(Integer userId, Integer productId, Integer orderItemId, String variantAttributesSnapshot, int rating, String comment) {
        if (userId == null) throw new IllegalArgumentException("Mã khách hàng không được để trống");
        if (productId == null) throw new IllegalArgumentException("Mã sản phẩm không được để trống");
        if (orderItemId == null) throw new IllegalArgumentException("Mã chi tiết đơn hàng không được để trống");
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Điểm số đánh giá phải nằm trong khoảng từ 1 đến 5 sao");

        this.userId = userId;
        this.productId = productId;
        this.orderItemId = orderItemId;
        this.variantAttributesSnapshot = variantAttributesSnapshot != null ? variantAttributesSnapshot : "Mặc định";
        this.rating = rating;
        this.comment = comment;
    }

    public void updateByUser(int rating, String comment) {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Điểm số đánh giá phải nằm trong khoảng từ 1 đến 5 sao");
        this.rating = rating;
        this.comment = comment;
        this.adminReply = null;
        this.status = ReviewStatus.PUBLISHED;
    }

    public void replyByAdmin(String reply) {
        if (reply == null || reply.trim().isEmpty()) throw new IllegalArgumentException("Nội dung phản hồi không được để trống");
        this.adminReply = reply;
    }

    public void changeStatus(ReviewStatus newStatus) {
        if (newStatus != null) {
            this.status = newStatus;
        }
    }

    public Integer getId() { return id; }
    public Integer getUserId() { return userId; }
    public Integer getProductId() { return productId; }
    public Integer getOrderItemId() { return orderItemId; }
    public String getVariantAttributesSnapshot() { return variantAttributesSnapshot; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getAdminReply() { return adminReply; }
    public ReviewStatus getStatus() { return status; }

    @Override
    public boolean equals(Object o) {
        return this == o || (o instanceof Review p && id != null && id.equals(p.id));
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}