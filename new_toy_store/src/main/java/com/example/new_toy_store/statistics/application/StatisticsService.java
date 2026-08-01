package com.example.new_toy_store.statistics.application;

import com.example.new_toy_store.logistics.domain.ShipmentRepository;
import com.example.new_toy_store.logistics.domain.ShipmentStatus;
import com.example.new_toy_store.order.domain.OrderRepository;
import com.example.new_toy_store.order.domain.OrderStatus;
import com.example.new_toy_store.payment.domain.PaymentMethod;
import com.example.new_toy_store.payment.domain.PaymentRefundRepository;
import com.example.new_toy_store.payment.domain.PaymentRepository;
import com.example.new_toy_store.payment.domain.PaymentStatus;
import com.example.new_toy_store.payment.domain.RefundStatus;
import com.example.new_toy_store.product.domain.InventoryRepository;
import com.example.new_toy_store.product.domain.ProductRepository;
import com.example.new_toy_store.product.domain.ProductStatus;
import com.example.new_toy_store.promotion.domain.PromotionRepository;
import com.example.new_toy_store.statistics.application.dto.response.InventoryStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.KpiMetricResponse;
import com.example.new_toy_store.statistics.application.dto.response.BreakdownStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.PaymentMethodStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.RevenueTrendPointResponse;
import com.example.new_toy_store.statistics.application.dto.response.StatisticPeriodResponse;
import com.example.new_toy_store.statistics.application.dto.response.StatisticsOverviewResponse;
import com.example.new_toy_store.statistics.application.dto.response.StatusCountResponse;
import com.example.new_toy_store.statistics.application.dto.response.TopSellingProductResponse;
import com.example.new_toy_store.statistics.domain.StatisticPeriod;
import com.example.new_toy_store.statistics.domain.StatisticDateField;
import com.example.new_toy_store.user.domain.UserRepository;
import com.example.new_toy_store.user.domain.UserStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsService {

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;
    private static final List<OrderStatus> REVENUE_ORDER_STATUSES = List.of(
            OrderStatus.COMPLETED,
            OrderStatus.PARTIALLY_REFUNDED,
            OrderStatus.FULLY_REFUNDED
    );

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository refundRepository;
    private final ShipmentRepository shipmentRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final PromotionRepository promotionRepository;

    public StatisticsService(
            OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            PaymentRefundRepository refundRepository,
            ShipmentRepository shipmentRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            InventoryRepository inventoryRepository,
            PromotionRepository promotionRepository
    ) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
        this.shipmentRepository = shipmentRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.promotionRepository = promotionRepository;
    }

    @Transactional(readOnly = true)
    public StatisticsOverviewResponse getOverview(StatisticPeriod period, int topLimit, int lowStockThreshold) {
        return getOverview(period, topLimit, lowStockThreshold, StatisticDateField.CREATED_AT);
    }

    @Transactional(readOnly = true)
    public StatisticsOverviewResponse getOverview(StatisticPeriod period, int topLimit, int lowStockThreshold, StatisticDateField dateField) {
        int safeTopLimit = Math.max(1, Math.min(topLimit, 20));
        int safeLowStockThreshold = Math.max(0, lowStockThreshold);
        StatisticPeriod previousPeriod = period.compareWithPreviousPeriod() ? period.previousPeriod() : null;

        return new StatisticsOverviewResponse(
                LocalDateTime.now(),
                new StatisticPeriodResponse(period),
                buildKpis(period, previousPeriod),
                buildRevenueTrend(period),
                buildOrderStatus(period, dateField == null ? StatisticDateField.CREATED_AT : dateField),
                buildPaymentStatus(period),
                buildRefundStatus(period),
                buildShipmentStatus(period),
                buildUserStatus(period),
                buildProductStatus(period),
                buildPaymentMethods(period),
                buildInventory(safeLowStockThreshold),
                getTopSellingProducts(period, safeTopLimit)
        );
    }

    @Transactional(readOnly = true)
    public List<TopSellingProductResponse> getTopSellingProducts(StatisticPeriod period, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        return orderRepository.findTopSellingProducts(
                        REVENUE_ORDER_STATUSES,
                        period.startDateTime(),
                        period.endExclusiveDateTime(),
                        PageRequest.of(0, safeLimit)
                )
                .stream()
                .map(this::toTopSellingProduct)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RevenueTrendPointResponse> getRevenueTrend(StatisticPeriod period) {
        return buildRevenueTrend(period);
    }

    @Transactional(readOnly = true)
    public List<PaymentMethodStatisticResponse> getPaymentMethods(StatisticPeriod period) {
        return buildPaymentMethods(period);
    }

    @Transactional(readOnly = true)
    public List<BreakdownStatisticResponse> getRevenueByCategory(StatisticPeriod period, int limit) {
        double totalAmount = orderRepository.sumTotalAmountByStatusesBetween(REVENUE_ORDER_STATUSES, period.startDateTime(), period.endExclusiveDateTime());
        return orderRepository.aggregateRevenueByCategory(
                        REVENUE_ORDER_STATUSES.stream().map(Enum::name).toList(),
                        period.startDateTime(),
                        period.endExclusiveDateTime(),
                        PageRequest.of(0, safeLimit(limit, 50))
                )
                .stream()
                .map(row -> breakdown(row, totalAmount))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BreakdownStatisticResponse> getRevenueByPromotion(StatisticPeriod period, int limit) {
        double totalAmount = orderRepository.sumTotalAmountByStatusesBetween(REVENUE_ORDER_STATUSES, period.startDateTime(), period.endExclusiveDateTime());
        return orderRepository.aggregateRevenueByPromotion(
                        REVENUE_ORDER_STATUSES,
                        period.startDateTime(),
                        period.endExclusiveDateTime(),
                        PageRequest.of(0, safeLimit(limit, 50))
                )
                .stream()
                .map(row -> breakdown(row, totalAmount))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BreakdownStatisticResponse> getTopSpendingCustomers(StatisticPeriod period, int limit) {
        double totalAmount = orderRepository.sumTotalAmountByStatusesBetween(REVENUE_ORDER_STATUSES, period.startDateTime(), period.endExclusiveDateTime());
        return orderRepository.findTopSpendingCustomers(
                        REVENUE_ORDER_STATUSES,
                        period.startDateTime(),
                        period.endExclusiveDateTime(),
                        PageRequest.of(0, safeLimit(limit, 50))
                )
                .stream()
                .map(row -> breakdown(row, totalAmount))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BreakdownStatisticResponse> getPaymentFailureReasons(StatisticPeriod period, int limit) {
        return paymentRepository.aggregateFailureReasons(
                        PaymentStatus.FAILED,
                        period.startDateTime(),
                        period.endExclusiveDateTime(),
                        PageRequest.of(0, safeLimit(limit, 50))
                )
                .stream()
                .map(row -> breakdown(row, 0))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BreakdownStatisticResponse> getRefundReasons(StatisticPeriod period, int limit) {
        double totalAmount = refundRepository.sumAmountByStatusBetween(RefundStatus.SUCCEEDED, period.startDateTime(), period.endExclusiveDateTime());
        return refundRepository.aggregateByReason(
                        period.startDateTime(),
                        period.endExclusiveDateTime(),
                        PageRequest.of(0, safeLimit(limit, 50))
                )
                .stream()
                .map(row -> breakdown(row, totalAmount))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BreakdownStatisticResponse> getShipmentsByProvider(StatisticPeriod period) {
        double totalFee = shipmentRepository.aggregateByProvider(period.startDateTime(), period.endExclusiveDateTime())
                .stream()
                .mapToDouble(row -> ((Number) row[3]).doubleValue())
                .sum();
        return shipmentRepository.aggregateByProvider(period.startDateTime(), period.endExclusiveDateTime())
                .stream()
                .map(row -> breakdown(row, totalFee))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BreakdownStatisticResponse> getShipmentFailureReasons(StatisticPeriod period, int limit) {
        return shipmentRepository.aggregateFailureReasons(
                        ShipmentStatus.DELIVERY_FAILED,
                        period.startDateTime(),
                        period.endExclusiveDateTime(),
                        PageRequest.of(0, safeLimit(limit, 50))
                )
                .stream()
                .map(row -> breakdown(row, 0))
                .toList();
    }

    public int getDefaultLowStockThreshold() {
        return DEFAULT_LOW_STOCK_THRESHOLD;
    }

    private int safeLimit(int limit, int max) {
        return Math.max(1, Math.min(limit, max));
    }

    private BreakdownStatisticResponse breakdown(Object[] row, double totalAmount) {
        return new BreakdownStatisticResponse(
                String.valueOf(row[0]),
                String.valueOf(row[1]),
                ((Number) row[2]).longValue(),
                ((Number) row[3]).doubleValue(),
                totalAmount
        );
    }

    private List<KpiMetricResponse> buildKpis(StatisticPeriod period, StatisticPeriod previousPeriod) {
        MetricValues current = readMetricValues(period);
        MetricValues previous = previousPeriod == null ? null : readMetricValues(previousPeriod);

        List<KpiMetricResponse> kpis = new ArrayList<>();
        kpis.add(kpi("GROSS_REVENUE", "Gross revenue", current.grossRevenue(), previous == null ? null : previous.grossRevenue()));
        kpis.add(kpi("REFUND_AMOUNT", "Refund amount", current.refundAmount(), previous == null ? null : previous.refundAmount()));
        kpis.add(kpi("NET_REVENUE", "Net revenue", current.netRevenue(), previous == null ? null : previous.netRevenue()));
        kpis.add(kpi("CREATED_ORDER_COUNT", "Created orders", current.createdOrderCount(), previous == null ? null : (double) previous.createdOrderCount()));
        kpis.add(kpi("SUCCESSFUL_ORDER_COUNT", "Successful orders", current.successfulOrderCount(), previous == null ? null : (double) previous.successfulOrderCount()));
        kpis.add(kpi("CANCELLED_ORDER_COUNT", "Cancelled orders", current.cancelledOrderCount(), previous == null ? null : (double) previous.cancelledOrderCount()));
        kpis.add(kpi("AVERAGE_ORDER_VALUE", "Average order value", current.averageOrderValue(), previous == null ? null : previous.averageOrderValue()));
        kpis.add(kpi("SOLD_QUANTITY", "Sold quantity", current.soldQuantity(), previous == null ? null : (double) previous.soldQuantity()));
        kpis.add(kpi("NEW_CUSTOMER_COUNT", "New customers", current.newCustomerCount(), previous == null ? null : (double) previous.newCustomerCount()));
        kpis.add(kpi("PAYMENT_SUCCESS_RATE", "Payment success rate", current.paymentSuccessRate(), previous == null ? null : previous.paymentSuccessRate()));
        kpis.add(kpi("DELIVERY_SUCCESS_RATE", "Delivery success rate", current.deliverySuccessRate(), previous == null ? null : previous.deliverySuccessRate()));
        kpis.add(kpi("REFUND_RATE", "Refund rate", current.refundRate(), previous == null ? null : previous.refundRate()));
        kpis.add(kpi("ACTIVE_PROMOTION_COUNT", "Active promotions", promotionRepository.countByIsActive(true), null));
        kpis.add(kpi("PROMOTION_USAGE_COUNT", "Promotion usage count", promotionRepository.sumUsedCount(), null));
        return kpis;
    }

    private MetricValues readMetricValues(StatisticPeriod period) {
        LocalDateTime from = period.startDateTime();
        LocalDateTime to = period.endExclusiveDateTime();

        double grossRevenue = orderRepository.sumTotalAmountByStatusesBetween(REVENUE_ORDER_STATUSES, from, to);
        double refundAmount = refundRepository.sumAmountByStatusBetween(RefundStatus.SUCCEEDED, from, to);
        long createdOrders = orderRepository.countCreatedBetween(from, to);
        long successfulOrders = orderRepository.countByStatusesBetween(REVENUE_ORDER_STATUSES, from, to);
        long cancelledOrders = orderRepository.countByStatusBetween(OrderStatus.CANCELLED, from, to);
        long soldQuantity = orderRepository.sumSoldQuantityBetween(REVENUE_ORDER_STATUSES, from, to);
        long newCustomers = userRepository.countCreatedBetween(from, to);
        long succeededPayments = paymentRepository.countByStatusBetween(PaymentStatus.SUCCEEDED, from, to);
        long failedPayments = paymentRepository.countByStatusBetween(PaymentStatus.FAILED, from, to);
        long deliveredShipments = shipmentRepository.countByStatusBetween(ShipmentStatus.DELIVERED, from, to);
        long failedShipments = shipmentRepository.countByStatusBetween(ShipmentStatus.DELIVERY_FAILED, from, to);
        long refundedOrders = orderRepository.countByStatusesBetween(List.of(OrderStatus.PARTIALLY_REFUNDED, OrderStatus.FULLY_REFUNDED), from, to);

        return new MetricValues(
                grossRevenue,
                refundAmount,
                createdOrders,
                successfulOrders,
                cancelledOrders,
                soldQuantity,
                newCustomers,
                rate(succeededPayments, succeededPayments + failedPayments),
                rate(deliveredShipments, deliveredShipments + failedShipments),
                rate(refundedOrders, successfulOrders)
        );
    }

    private List<RevenueTrendPointResponse> buildRevenueTrend(StatisticPeriod period) {
        Map<String, TrendAccumulator> buckets = createBuckets(period);
        applyDailyRevenueRows(buckets, period);
        applyDailyRefundRows(buckets, period);

        return buckets.entrySet().stream()
                .map(entry -> new RevenueTrendPointResponse(
                        entry.getKey(),
                        entry.getValue().grossRevenue(),
                        entry.getValue().refundAmount(),
                        entry.getValue().orderCount()
                ))
                .toList();
    }

    private void applyDailyRevenueRows(Map<String, TrendAccumulator> buckets, StatisticPeriod period) {
        List<Object[]> rows = orderRepository.aggregateDailyRevenue(
                REVENUE_ORDER_STATUSES,
                period.startDateTime(),
                period.endExclusiveDateTime()
        );
        for (Object[] row : rows) {
            String bucketKey = bucketKey(toLocalDate(row[0]), period);
            TrendAccumulator accumulator = buckets.get(bucketKey);
            if (accumulator != null) {
                accumulator.addRevenue(((Number) row[2]).doubleValue(), ((Number) row[1]).longValue());
            }
        }
    }

    private void applyDailyRefundRows(Map<String, TrendAccumulator> buckets, StatisticPeriod period) {
        List<Object[]> rows = refundRepository.aggregateDailyRefundAmount(
                RefundStatus.SUCCEEDED,
                period.startDateTime(),
                period.endExclusiveDateTime()
        );
        for (Object[] row : rows) {
            String bucketKey = bucketKey(toLocalDate(row[0]), period);
            TrendAccumulator accumulator = buckets.get(bucketKey);
            if (accumulator != null) {
                accumulator.addRefund(((Number) row[1]).doubleValue());
            }
        }
    }

    private Map<String, TrendAccumulator> createBuckets(StatisticPeriod period) {
        Map<String, TrendAccumulator> buckets = new LinkedHashMap<>();
        LocalDate cursor = bucketStart(period.from(), period);
        while (!cursor.isAfter(period.to())) {
            buckets.put(bucketKey(cursor, period), new TrendAccumulator());
            cursor = nextBucket(cursor, period);
        }
        return buckets;
    }

    private List<StatusCountResponse> buildOrderStatus(StatisticPeriod period, StatisticDateField dateField) {
        return Arrays.stream(OrderStatus.values())
                .map(status -> statusCount(status.name(), status.getDisplayName(), countOrdersByDateField(status, period, dateField)))
                .toList();
    }

    private long countOrdersByDateField(OrderStatus status, StatisticPeriod period, StatisticDateField dateField) {
        if (dateField == StatisticDateField.COMPLETED_AT) {
            return status == OrderStatus.COMPLETED ? orderRepository.countHistoryByStatusBetween(OrderStatus.COMPLETED, period.startDateTime(), period.endExclusiveDateTime()) : 0;
        }
        if (dateField == StatisticDateField.CANCELLED_AT) {
            return status == OrderStatus.CANCELLED ? orderRepository.countHistoryByStatusBetween(OrderStatus.CANCELLED, period.startDateTime(), period.endExclusiveDateTime()) : 0;
        }
        return orderRepository.countByStatusBetween(status, period.startDateTime(), period.endExclusiveDateTime());
    }

    private List<StatusCountResponse> buildPaymentStatus(StatisticPeriod period) {
        return Arrays.stream(PaymentStatus.values())
                .map(status -> statusCount(status.name(), status.getDisplayName(), paymentRepository.countByStatusBetween(status, period.startDateTime(), period.endExclusiveDateTime())))
                .toList();
    }

    private List<StatusCountResponse> buildRefundStatus(StatisticPeriod period) {
        return Arrays.stream(RefundStatus.values())
                .map(status -> statusCount(status.name(), status.getDisplayName(), refundRepository.countByStatusBetween(status, period.startDateTime(), period.endExclusiveDateTime())))
                .toList();
    }

    private List<StatusCountResponse> buildShipmentStatus(StatisticPeriod period) {
        return Arrays.stream(ShipmentStatus.values())
                .map(status -> statusCount(status.name(), status.getDisplayName(), shipmentRepository.countByStatusBetween(status, period.startDateTime(), period.endExclusiveDateTime())))
                .toList();
    }

    private List<StatusCountResponse> buildUserStatus(StatisticPeriod period) {
        return Arrays.stream(UserStatus.values())
                .map(status -> statusCount(status.name(), status.getDisplayName(), userRepository.countByStatusBetween(status, period.startDateTime(), period.endExclusiveDateTime())))
                .toList();
    }

    private List<StatusCountResponse> buildProductStatus(StatisticPeriod period) {
        return Arrays.stream(ProductStatus.values())
                .map(status -> statusCount(status.name(), status.getDisplayName(), productRepository.countByStatusBetween(status, period.startDateTime(), period.endExclusiveDateTime())))
                .toList();
    }

    private List<PaymentMethodStatisticResponse> buildPaymentMethods(StatisticPeriod period) {
        double totalAmount = paymentRepository.sumAmountByStatusBetween(
                PaymentStatus.SUCCEEDED,
                period.startDateTime(),
                period.endExclusiveDateTime()
        );
        return paymentRepository.aggregateAmountByMethod(
                        PaymentStatus.SUCCEEDED,
                        period.startDateTime(),
                        period.endExclusiveDateTime()
                )
                .stream()
                .map(row -> new PaymentMethodStatisticResponse(
                        ((PaymentMethod) row[0]).name(),
                        ((Number) row[1]).longValue(),
                        ((Number) row[2]).doubleValue(),
                        totalAmount
                ))
                .toList();
    }

    private InventoryStatisticResponse buildInventory(int lowStockThreshold) {
        return new InventoryStatisticResponse(
                inventoryRepository.sumStockQuantity(),
                inventoryRepository.sumReservedQuantity(),
                inventoryRepository.countLowStock(lowStockThreshold)
        );
    }

    private TopSellingProductResponse toTopSellingProduct(Object[] row) {
        return new TopSellingProductResponse(
                (Integer) row[0],
                (String) row[1],
                ((Number) row[2]).longValue(),
                ((Number) row[3]).longValue(),
                ((Number) row[4]).doubleValue()
        );
    }

    private KpiMetricResponse kpi(String code, String label, double value, Double previousValue) {
        return new KpiMetricResponse(code, label, value, previousValue);
    }

    private StatusCountResponse statusCount(String code, String label, long count) {
        return new StatusCountResponse(code, label, count);
    }

    private double rate(long numerator, long denominator) {
        if (denominator <= 0) {
            return 0.0;
        }
        return Math.round(((double) numerator / denominator) * 10_000.0) / 100.0;
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private String bucketKey(LocalDate date, StatisticPeriod period) {
        return period.appliedGroupBy().bucketKey(date);
    }

    private LocalDate bucketStart(LocalDate date, StatisticPeriod period) {
        return period.appliedGroupBy().bucketStart(date);
    }

    private LocalDate nextBucket(LocalDate date, StatisticPeriod period) {
        return period.appliedGroupBy().nextBucket(date);
    }

    private record MetricValues(
            double grossRevenue,
            double refundAmount,
            long createdOrderCount,
            long successfulOrderCount,
            long cancelledOrderCount,
            long soldQuantity,
            long newCustomerCount,
            double paymentSuccessRate,
            double deliverySuccessRate,
            double refundRate
    ) {
        double netRevenue() {
            return Math.max(0.0, Math.round((grossRevenue - refundAmount) * 100.0) / 100.0);
        }

        double averageOrderValue() {
            if (successfulOrderCount <= 0) {
                return 0.0;
            }
            return Math.round((netRevenue() / successfulOrderCount) * 100.0) / 100.0;
        }
    }

    private static class TrendAccumulator {
        private double grossRevenue;
        private double refundAmount;
        private long orderCount;

        void addRevenue(double amount, long count) {
            this.grossRevenue += amount;
            this.orderCount += count;
        }

        void addRefund(double amount) {
            this.refundAmount += amount;
        }

        double grossRevenue() { return grossRevenue; }
        double refundAmount() { return refundAmount; }
        long orderCount() { return orderCount; }
    }
}
