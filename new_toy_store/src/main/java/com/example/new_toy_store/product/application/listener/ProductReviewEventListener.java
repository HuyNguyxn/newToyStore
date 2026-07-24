package com.example.new_toy_store.product.application.listener;

import com.example.new_toy_store.global.event.ProductReviewRatingChangedEvent;
import com.example.new_toy_store.product.application.service.ProductService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ProductReviewEventListener {

    private final ProductService productService;

    public ProductReviewEventListener(ProductService productService) {
        this.productService = productService;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void onProductReviewRatingChanged(ProductReviewRatingChangedEvent event) {
        productService.updateProductRating(
                event.productId(),
                event.averageRating(),
                event.reviewCount()
        );
    }
}
