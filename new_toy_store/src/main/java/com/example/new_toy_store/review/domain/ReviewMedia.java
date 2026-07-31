package com.example.new_toy_store.review.domain;

import com.example.new_toy_store.global.common.BaseSoftDeleteEntity;
import com.example.new_toy_store.review.domain.exception.InvalidReviewOperationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "review_media",
        indexes = {
                @Index(name = "idx_review_media_review_id", columnList = "review_id"),
                @Index(name = "idx_review_media_type", columnList = "media_type")
        }
)
public class ReviewMedia extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 20)
    private ReviewMediaType mediaType;

    @Column(name = "media_url", nullable = false, length = 1000)
    private String mediaUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected ReviewMedia() {
    }

    public ReviewMedia(ReviewMediaType mediaType, String mediaUrl, int displayOrder) {
        if (mediaType == null) {
            throw InvalidReviewOperationException.missingRequirement("mediaType");
        }
        if (mediaUrl == null || mediaUrl.trim().isEmpty()) {
            throw InvalidReviewOperationException.emptyMediaUrl();
        }
        this.mediaType = mediaType;
        this.mediaUrl = mediaUrl.trim();
        this.displayOrder = Math.max(0, displayOrder);
    }

    void assignToReview(Review review) {
        this.review = review;
    }

    public Integer getId() {
        return id;
    }

    public ReviewMediaType getMediaType() {
        return mediaType;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
