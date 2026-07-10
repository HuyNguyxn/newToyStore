package com.example.new_toy_store.infrastructure.schedule;

import com.example.new_toy_store.order.application.OrderService;
import com.example.new_toy_store.order.domain.Order;
import com.example.new_toy_store.order.domain.OrderRepository;
import com.example.new_toy_store.order.domain.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderTimeoutScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutScheduler.class);

    private final OrderRepository repository;
    private final OrderService orderService;
    private final int expirationMinutes;

    public OrderTimeoutScheduler(
            OrderRepository repository,
            OrderService orderService,
            @Value("${app.order.timeout.expiration-minutes:30}") int expirationMinutes) {
        this.repository = repository;
        this.orderService = orderService;
        this.expirationMinutes = expirationMinutes;
    }

    @Scheduled(cron = "${app.order.timeout.cron}", zone = "${app.order.timeout.zone}")
    @Transactional
    public void executeTimeoutCheck() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(expirationMinutes);
        List<Order> expiredOrders = repository.findExpiredOrders(OrderStatus.PENDING, cutoffTime);

        if (!expiredOrders.isEmpty()) {
            log.info("Phát hiện {} đơn hàng PENDING hết hạn ({} phút). Đang tự động hủy và hoàn kho...", expiredOrders.size(), expirationMinutes);
            for (Order order : expiredOrders) {
                try {
                    orderService.cancel(order.getId(), "Hệ thống tự động hủy do hết hạn xác nhận/thanh toán (" + expirationMinutes + " phút)");
                    log.info("Đã hủy tự động đơn hàng ID: {}", order.getId());
                } catch (Exception e) {
                    log.error("Lỗi khi hủy tự động đơn hàng ID: {}", order.getId(), e);
                }
            }
        }
    }
}