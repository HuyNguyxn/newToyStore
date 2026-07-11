package com.example.new_toy_store.review.application;

import com.example.new_toy_store.infrastructure.specification.ReviewSpecification;
import com.example.new_toy_store.moderation.application.BlacklistWordService;
import com.example.new_toy_store.order.application.OrderService;
import com.example.new_toy_store.order.domain.OrderItem;
import com.example.new_toy_store.product.application.ProductService;
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
import com.example.new_toy_store.review.mapper.ReviewMapper;
import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final OrderService orderService;
    private final ProductService productService;
    private final UserRepository userRepository;
    private final BlacklistWordService blacklistWordService;

    public ReviewService(ReviewRepository repository,
                         OrderService orderService,
                         ProductService productService,
                         UserRepository userRepository,
                         BlacklistWordService blacklistWordService) {
        this.repository = repository;
        this.orderService = orderService;
        this.productService = productService;
        this.userRepository = userRepository;
        this.blacklistWordService = blacklistWordService;
    }

    @Transactional
    public ReviewResponse createReview(Integer userId, ReviewCreateRequest request) {
        User user = validateUser(userId);
        if (blacklistWordService.containsBadWord(request.getComment())) {
            throw new IllegalArgumentException("Nội dung đánh giá chứa từ ngữ vi phạm tiêu chuẩn cộng đồng. Vui lòng chỉnh sửa lại.");
        }

        OrderItem orderItem = orderService.getCompletedOrderItemForReview(request.getOrderItemId(), userId);

        if (orderItem.getUpdatedAt() != null && orderItem.getUpdatedAt().plusDays(REVIEW_TIME_WINDOW_DAYS).isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Đã quá thời hạn " + REVIEW_TIME_WINDOW_DAYS + " ngày để đánh giá sản phẩm này kể từ khi nhận hàng.");
        }

        if (repository.findByOrderItemId(request.getOrderItemId()).isPresent()) {
            throw new IllegalStateException("Bạn đã đánh giá sản phẩm này trong đơn hàng rồi. Vui lòng sử dụng tính năng chỉnh sửa.");
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

        syncProductRating(orderItem.getProductId());
        return ReviewMapper.toResponse(review, user, null);
    }

    @Transactional
    public ReviewResponse updateReview(Integer userId, Integer reviewId, ReviewUpdateRequest request) {
        User user = validateUser(userId);
        if (blacklistWordService.containsBadWord(request.getComment())) {
            throw new IllegalArgumentException("Nội dung đánh giá chứa từ ngữ vi phạm tiêu chuẩn cộng đồng. Vui lòng chỉnh sửa lại.");
        }

        Review review = getReviewEntity(reviewId);

        if (!review.getUserId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền chỉnh sửa đánh giá của người khác");
        }

        review.updateByUser(request.getRating(), request.getComment());
        repository.save(review);

        syncProductRating(review.getProductId());
        return ReviewMapper.toResponse(review, user, null);
    }

    @Transactional
    public void deleteMyReview(Integer userId, Integer reviewId) {
        validateUser(userId);
        Review review = getReviewEntity(reviewId);

        if (!review.getUserId().equals(userId)) {
            throw new IllegalStateException("Bạn không có quyền xóa đánh giá của người khác");
        }

        review.delete();
        repository.save(review);
        syncProductRating(review.getProductId());
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
            throw new IllegalArgumentException("Bộ lọc sao chỉ chấp nhận giá trị từ 1 đến 5");
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
        ReviewStatus newStatus = ReviewStatus.from(statusStr);
        review.changeStatus(newStatus);
        repository.save(review);
        syncProductRating(review.getProductId());
    }

    @Transactional
    public void replyToReview(Integer id, AdminReplyRequest request) {
        Review review = getReviewEntity(id);
        review.replyByAdmin(request.getReply());
        repository.save(review);
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

        Map<Integer, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        Map<Integer, Product> productMap = productService.getProductsByIdsWithDetails(productIds).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        return reviewPage.map(review -> ReviewMapper.toResponse(
                review,
                userMap.get(review.getUserId()),
                productMap.get(review.getProductId())
        ));
    }

    private void syncProductRating(Integer productId) {
        Double avgRating = repository.calculateAverageRating(productId);
        Integer totalReviews = repository.countPublishedReviews(productId);
        double rawAvg = (avgRating != null) ? avgRating : 0.0;
        int count = (totalReviews != null) ? totalReviews : 0;
        double safeAverage = Math.max(0.0, Math.round(rawAvg * 10.0) / 10.0);
        productService.updateProductRating(productId, safeAverage, count);
    }

    private User validateUser(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin khách hàng trên hệ thống"));
        if (!user.getStatus().canModifyData()) {
            throw new IllegalStateException("Thao tác bị từ chối. Tài khoản của bạn hiện đang ở trạng thái: " + user.getStatus().getDisplayName());
        }
        return user;
    }

    private Review getReviewEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dữ liệu đánh giá"));
    }
}