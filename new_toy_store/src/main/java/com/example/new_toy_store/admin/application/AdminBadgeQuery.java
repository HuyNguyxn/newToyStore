package com.example.new_toy_store.admin.application;

import com.example.new_toy_store.admin.application.dto.response.AdminMenuBadgeResponse;
import com.example.new_toy_store.customer_return.domain.CustomerReturnRepository;
import com.example.new_toy_store.customer_return.domain.CustomerReturnStatus;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentRepository;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentStatus;
import com.example.new_toy_store.imports.domain.ImportNoteRepository;
import com.example.new_toy_store.imports.domain.ImportStatus;
import com.example.new_toy_store.order.domain.OrderRepository;
import com.example.new_toy_store.order.domain.OrderStatus;
import com.example.new_toy_store.product.domain.InventoryRepository;
import com.example.new_toy_store.review.domain.ReviewRepository;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnRepository;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminBadgeQuery {
    private final OrderRepository orderRepository;
    private final CustomerReturnRepository customerReturnRepository;
    private final SupplierReturnRepository supplierReturnRepository;
    private final InventoryRepository inventoryRepository;
    private final ImportNoteRepository importNoteRepository;
    private final CustomerPaymentRepository customerPaymentRepository;
    private final ReviewRepository reviewRepository;

    public AdminBadgeQuery(
            OrderRepository orderRepository,
            CustomerReturnRepository customerReturnRepository,
            SupplierReturnRepository supplierReturnRepository,
            InventoryRepository inventoryRepository,
            ImportNoteRepository importNoteRepository,
            CustomerPaymentRepository customerPaymentRepository,
            ReviewRepository reviewRepository
    ) {
        this.orderRepository = orderRepository;
        this.customerReturnRepository = customerReturnRepository;
        this.supplierReturnRepository = supplierReturnRepository;
        this.inventoryRepository = inventoryRepository;
        this.importNoteRepository = importNoteRepository;
        this.customerPaymentRepository = customerPaymentRepository;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public AdminMenuBadgeResponse getMenuBadges() {
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minusDays(30);
        List<String> revenueStatuses = List.of(
                OrderStatus.COMPLETED.name(),
                OrderStatus.PARTIALLY_REFUNDED.name(),
                OrderStatus.FULLY_REFUNDED.name()
        );

        return new AdminMenuBadgeResponse(
                orderRepository.countByStatus(OrderStatus.PENDING),
                customerReturnRepository.countByStatus(CustomerReturnStatus.REQUESTED),
                supplierReturnRepository.countByStatus(SupplierReturnStatus.PENDING_APPROVAL),
                importNoteRepository.countByStatus(ImportStatus.PENDING),
                inventoryRepository.countLowStock(5),
                customerPaymentRepository.countCustomerByStatus(CustomerPaymentStatus.PENDING),
                orderRepository.countHistoryByStatusBetween(OrderStatus.CANCELLED, from, to),
                orderRepository.countSlowSellingProducts(revenueStatuses, from, to, 5),
                reviewRepository.countUnansweredReviewsBelowRating(3)
        );
    }
}
