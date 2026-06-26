package com.example.new_toy_store.review.api;

import com.example.new_toy_store.review.application.ReviewService;
import com.example.new_toy_store.review.application.dto.request.AdminReplyRequest;
import com.example.new_toy_store.review.application.dto.request.ReviewCreateRequest;
import com.example.new_toy_store.review.application.dto.request.ReviewUpdateRequest;
import com.example.new_toy_store.review.application.dto.response.ReviewResponse;
import com.example.new_toy_store.review.application.dto.response.ReviewSummaryResponse;
import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reviews")
@Validated
public class ReviewController {

    private final ReviewService service;
    private final UserRepository userRepository;

    public ReviewController(ReviewService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ReviewResponse createReview(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ReviewCreateRequest request) {
        return service.createReview(getAuthenticatedUserId(userDetails), request);
    }

    @PutMapping("/{id}")
    public ReviewResponse updateReview(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id, @Valid @RequestBody ReviewUpdateRequest request) {
        return service.updateReview(getAuthenticatedUserId(userDetails), id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteMyReview(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer id) {
        service.deleteMyReview(getAuthenticatedUserId(userDetails), id);
    }

    @GetMapping("/me")
    public Page<ReviewResponse> getMyReviews(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        return service.getMyReviews(getAuthenticatedUserId(userDetails), pageable);
    }

    @GetMapping("/products/{productId}/summary")
    public ReviewSummaryResponse getProductReviewSummary(@PathVariable Integer productId) {
        return service.getProductReviewSummary(productId);
    }

    @GetMapping("/products/{productId}")
    public Page<ReviewResponse> getPublicReviews(
            @PathVariable Integer productId,
            @RequestParam(required = false) Integer rating,
            Pageable pageable) {
        return service.getPublicReviewsForProduct(productId, rating, pageable);
    }

    @GetMapping("/admin/products/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<ReviewResponse> getAllReviewsForAdmin(@PathVariable Integer productId, Pageable pageable) {
        return service.getAllReviewsForAdmin(productId, pageable);
    }

    @PatchMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public void changeStatus(@PathVariable Integer id, @RequestParam String status) {
        service.changeReviewStatus(id, status);
    }

    @PatchMapping("/admin/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public void replyToReview(@PathVariable Integer id, @Valid @RequestBody AdminReplyRequest request) {
        service.replyToReview(id, request);
    }

    private Integer getAuthenticatedUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy định danh người dùng"));
        return user.getId();
    }
}