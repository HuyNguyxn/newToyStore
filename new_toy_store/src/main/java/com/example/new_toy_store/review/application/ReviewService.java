package com.example.new_toy_store.review.application;

import com.example.new_toy_store.global.event.ProductReviewRatingChangedEvent;
import com.example.new_toy_store.global.event.ReviewDeletedEvent;
import com.example.new_toy_store.global.event.ReviewRepliedEvent;
import com.example.new_toy_store.global.event.ReviewStatusChangedEvent;
import com.example.new_toy_store.infrastructure.specification.ReviewSpecification;
import com.example.new_toy_store.moderation.application.facade.ModerationFacade;
import com.example.new_toy_store.order.application.facade.OrderFacade;
import com.example.new_toy_store.order.domain.OrderItem;
import com.example.new_toy_store.product.application.facade.ProductFacade;
import com.example.new_toy_store.product.domain.Product;
import com.example.new_toy_store.review.application.dto.request.AdminReplyRequest;
import com.example.new_toy_store.review.application.dto.request.ReviewCreateRequest;
import com.example.new_toy_store.review.application.dto.request.ReviewFilterRequest;
import com.example.new_toy_store.review.application.dto.request.ReviewUpdateRequest;
import com.example.new_toy_store.review.application.dto.response.ReviewResponse;
import com.example.new_toy_store.review.application.dto.response.ReviewSummaryResponse;
import com.example.new_toy_store.review.domain.Review;
import com.example.new_toy_store.review.domain.ReviewRepository;
import com.example.new_toy_store.review.domain.ReviewStatus;
import com.example.new_toy_store.review.domain.exception.InvalidReviewOperationException;
import com.example.new_toy_store.review.domain.exception.ReviewAccessDeniedException;
import com.example.new_toy_store.review.domain.exception.ReviewConflictException;
import com.example.new_toy_store.review.domain.exception.ReviewNotFoundException;
import com.example.new_toy_store.review.mapper.ReviewMapper;
import com.example.new_toy_store.user.application.UserFacade;
import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.exception.UserNotFoundException;
import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private static final int REVIEW_TIME_WINDOW_DAYS = 7;

    private final ReviewRepository repository;
    private final OrderFacade orderFacade;
    private final ProductFacade productFacade;
    private final UserFacade userFacade;
    private final ModerationFacade moderationFacade;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

    public ReviewService(ReviewRepository repository,
                         OrderFacade orderFacade,
                         ProductFacade productFacade,
                         UserFacade userFacade,
                         ModerationFacade moderationFacade,
                         ApplicationEventPublisher eventPublisher,
                         EntityManager entityManager) {
        this.repository = repository;
        this.orderFacade = orderFacade;
        this.productFacade = productFacade;
        this.userFacade = userFacade;
        this.moderationFacade = moderationFacade;
        this.eventPublisher = eventPublisher;
        this.entityManager = entityManager;
    }

    @Transactional
    public ReviewResponse createReview(Integer userId, ReviewCreateRequest request) {
        User user = validateUser(userId);

        if (moderationFacade.containsProhibitedWord(request.getComment())) {
            throw InvalidReviewOperationException.prohibitedContent("comment");
        }

        OrderItem orderItem = orderFacade.getCompletedOrderItemForReview(request.getOrderItemId(), userId);

        if (orderItem.getUpdatedAt() != null
                && orderItem.getUpdatedAt().plusDays(REVIEW_TIME_WINDOW_DAYS).isBefore(LocalDateTime.now())) {
            throw InvalidReviewOperationException.timeWindowExpired(REVIEW_TIME_WINDOW_DAYS);
        }

        if (repository.findByOrderItemId(request.getOrderItemId()).isPresent()) {
            throw ReviewConflictException.duplicateReview(request.getOrderItemId());
        }

        Review review = new Review(
                userId,
                orderItem.getProductId(),
                orderItem.getId(),
                orderItem.getVariantAttributesSnapshot(),
                request.getRating(),
                request.getComment()
        );
        repository.save(review);

        publishProductRatingChanged(orderItem.getProductId());
        return ReviewMapper.toResponse(review, user, null);
    }

    @Transactional
    public ReviewResponse updateReview(Integer userId, Integer reviewId, ReviewUpdateRequest request) {
        User user = validateUser(userId);

        if (moderationFacade.containsProhibitedWord(request.getComment())) {
            throw InvalidReviewOperationException.prohibitedContent("comment");
        }

        Review review = getReviewEntity(reviewId);

        if (!review.getUserId().equals(userId)) {
            throw ReviewAccessDeniedException.notOwner(userId);
        }

        review.updateByUser(request.getRating(), request.getComment());
        repository.save(review);

        publishProductRatingChanged(review.getProductId());
        return ReviewMapper.toResponse(review, user, null);
    }

    @Transactional
    public void deleteMyReview(Integer userId, Integer reviewId) {
        validateUser(userId);
        Review review = getReviewEntity(reviewId);

        if (!review.getUserId().equals(userId)) {
            throw ReviewAccessDeniedException.notOwner(userId);
        }

        Integer productId = review.getProductId();
        entityManager.detach(review);
        int updatedRows = repository.softDeleteWithVersion(reviewId, review.getVersion());
        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(Review.class, reviewId);
        }
        eventPublisher.publishEvent(ReviewDeletedEvent.now(review.getId(), review.getUserId(), productId));
        publishProductRatingChanged(review.getProductId());
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getMyReviews(Integer userId, Pageable pageable) {
        Page<Review> reviewPage = repository.findByUserId(userId, pageable);
        return mapReviewsToResponsesWithBatchData(reviewPage);
    }

    @Transactional(readOnly = true)
    public ReviewSummaryResponse getProductReviewSummary(Integer productId) {
        Double avgRating = repository.calculateAverageRating(productId);
        Integer totalReviews = repository.countPublishedReviews(productId);

        double safeAverage = Math.max(0.0, Math.round((avgRating != null ? avgRating : 0.0) * 10.0) / 10.0);
        int total = totalReviews != null ? totalReviews : 0;

        List<Object[]> rawCounts = repository.countReviewsByRatingGroup(productId);
        Map<Integer, Integer> starCounts = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            starCounts.put(i, 0);
        }
        for (Object[] row : rawCounts) {
            Integer star = (Integer) row[0];
            Long count = (Long) row[1];
            starCounts.put(star, count.intValue());
        }

        return new ReviewSummaryResponse(safeAverage, total, starCounts);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getPublicReviewsForProduct(Integer productId, Integer ratingFilter, Pageable pageable) {
        if (ratingFilter != null && (ratingFilter < 1 || ratingFilter > 5)) {
            throw InvalidReviewOperationException.invalidRating(ratingFilter);
        }
        Page<Review> reviewPage = repository.findPublicReviewsWithFilter(productId, ratingFilter, pageable);
        return mapReviewsToResponsesWithBatchData(reviewPage);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviewsForAdmin(Integer productId, Pageable pageable) {
        Page<Review> reviewPage = repository.findByProductId(productId, pageable);
        return mapReviewsToResponsesWithBatchData(reviewPage);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> filterGlobalReviewsForAdmin(ReviewFilterRequest filterRequest, Pageable pageable) {
        Specification<Review> spec = ReviewSpecification.filter(filterRequest);
        Page<Review> reviewPage = repository.findAll(spec, pageable);
        return mapReviewsToResponsesWithBatchData(reviewPage);
    }

    @Transactional
    public void changeReviewStatus(Integer id, String statusStr) {
        Review review = getReviewEntity(id);
        ReviewStatus previousStatus = review.getStatus();
        ReviewStatus newStatus = ReviewStatus.from(statusStr);
        review.changeStatus(newStatus);
        entityManager.detach(review);
        int updatedRows = repository.updateStatusWithVersion(id, review.getVersion(), newStatus);
        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(Review.class, id);
        }
        if (previousStatus != newStatus) {
            eventPublisher.publishEvent(ReviewStatusChangedEvent.now(
                    review.getId(),
                    review.getUserId(),
                    review.getProductId(),
                    previousStatus,
                    newStatus
            ));
        }
        publishProductRatingChanged(review.getProductId());
    }

    @Transactional
    public void replyToReview(Integer id, AdminReplyRequest request) {
        Review review = getReviewEntity(id);
        review.replyByAdmin(request.getReply());
        entityManager.detach(review);
        int updatedRows = repository.updateAdminReplyWithVersion(id, review.getVersion(), request.getReply());
        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(Review.class, id);
        }
        eventPublisher.publishEvent(ReviewRepliedEvent.now(review.getId(), review.getUserId(), review.getProductId()));
    }

    private Page<ReviewResponse> mapReviewsToResponsesWithBatchData(Page<Review> reviewPage) {
        if (reviewPage.isEmpty()) {
            return reviewPage.map(review -> ReviewMapper.toResponse(review, null, null));
        }

        Set<Integer> userIds = reviewPage.getContent().stream()
                .map(Review::getUserId)
                .collect(Collectors.toSet());

        Set<Integer> productIds = reviewPage.getContent().stream()
                .map(Review::getProductId)
                .collect(Collectors.toSet());

        Map<Integer, User> userMap = userFacade.getUsersByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        Map<Integer, Product> productMap = productFacade.getProductsByIdsWithDetails(productIds).stream()
                .collect(Collectors.toMap(Product::getId, product -> product));

        return reviewPage.map(review -> ReviewMapper.toResponse(
                review,
                userMap.get(review.getUserId()),
                productMap.get(review.getProductId())
        ));
    }

    private void publishProductRatingChanged(Integer productId) {
        Double avgRating = repository.calculateAverageRating(productId);
        Integer totalReviews = repository.countPublishedReviews(productId);
        double rawAvg = (avgRating != null) ? avgRating : 0.0;
        int count = (totalReviews != null) ? totalReviews : 0;
        double safeAverage = Math.max(0.0, Math.round(rawAvg * 10.0) / 10.0);
        eventPublisher.publishEvent(ProductReviewRatingChangedEvent.now(productId, safeAverage, count));
    }

    private User validateUser(Integer userId) {
        User user;
        try {
            user = userFacade.getRequiredUser(userId);
        } catch (UserNotFoundException ex) {
            throw ReviewAccessDeniedException.userNotFound(userId);
        }
        if (!user.getStatus().canModifyData()) {
            throw ReviewAccessDeniedException.userAccountLocked(userId, user.getStatus().getDisplayName());
        }
        return user;
    }

    private Review getReviewEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));
    }
}
