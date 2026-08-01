package com.example.new_toy_store.statistics.api;

import com.example.new_toy_store.statistics.application.facade.StatisticsFacade;
import com.example.new_toy_store.statistics.application.dto.request.StatisticsOverviewRequest;
import com.example.new_toy_store.statistics.application.dto.request.TopSellingProductsRequest;
import com.example.new_toy_store.statistics.application.dto.response.BreakdownStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.PaymentMethodStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.RevenueTrendPointResponse;
import com.example.new_toy_store.statistics.application.dto.response.StatisticsOverviewResponse;
import com.example.new_toy_store.statistics.application.dto.response.TopSellingProductResponse;
import com.example.new_toy_store.statistics.domain.StatisticPeriod;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statistics")
@Validated
@PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
public class StatisticsController {

    private final StatisticsFacade facade;

    public StatisticsController(StatisticsFacade facade) {
        this.facade = facade;
    }

    @GetMapping("/overview")
    public StatisticsOverviewResponse getOverview(@Valid @ModelAttribute StatisticsOverviewRequest request) {
        StatisticPeriod period = StatisticPeriod.of(
                request.getFrom(), request.getTo(), request.getTimezone(),
                request.getGroupBy(), request.isCompareWithPreviousPeriod()
        );
        return facade.getOverview(period, request.getTopLimit(), request.getLowStockThreshold(), request.getDateField());
    }

    @GetMapping("/products/top-selling")
    public List<TopSellingProductResponse> getTopSellingProducts(
            @Valid @ModelAttribute TopSellingProductsRequest request
    ) {
        StatisticPeriod period = StatisticPeriod.of(
                request.getFrom(), request.getTo(), request.getTimezone(), request.getGroupBy(), false
        );
        return facade.getTopSellingProducts(period, request.getLimit());
    }

    @GetMapping("/revenue/trend")
    public List<RevenueTrendPointResponse> getRevenueTrend(@Valid @ModelAttribute StatisticsOverviewRequest request) {
        return facade.getRevenueTrend(toPeriod(request));
    }

    @GetMapping("/revenue/by-payment-method")
    public List<PaymentMethodStatisticResponse> getRevenueByPaymentMethod(@Valid @ModelAttribute StatisticsOverviewRequest request) {
        return facade.getPaymentMethods(toPeriod(request));
    }

    @GetMapping("/revenue/by-category")
    public List<BreakdownStatisticResponse> getRevenueByCategory(@Valid @ModelAttribute StatisticsOverviewRequest request) {
        return facade.getRevenueByCategory(toPeriod(request), request.getTopLimit());
    }

    @GetMapping("/revenue/by-product")
    public List<TopSellingProductResponse> getRevenueByProduct(@Valid @ModelAttribute TopSellingProductsRequest request) {
        StatisticPeriod period = StatisticPeriod.of(
                request.getFrom(), request.getTo(), request.getTimezone(), request.getGroupBy(), false
        );
        return facade.getTopSellingProducts(period, request.getLimit());
    }

    @GetMapping("/revenue/by-promotion")
    public List<BreakdownStatisticResponse> getRevenueByPromotion(@Valid @ModelAttribute StatisticsOverviewRequest request) {
        return facade.getRevenueByPromotion(toPeriod(request), request.getTopLimit());
    }

    @GetMapping("/customers/top-spending")
    public List<BreakdownStatisticResponse> getTopSpendingCustomers(@Valid @ModelAttribute StatisticsOverviewRequest request) {
        return facade.getTopSpendingCustomers(toPeriod(request), request.getTopLimit());
    }

    @GetMapping("/payments/failure-reasons")
    public List<BreakdownStatisticResponse> getPaymentFailureReasons(@Valid @ModelAttribute StatisticsOverviewRequest request) {
        return facade.getPaymentFailureReasons(toPeriod(request), request.getTopLimit());
    }

    @GetMapping("/refunds/by-reason")
    public List<BreakdownStatisticResponse> getRefundReasons(@Valid @ModelAttribute StatisticsOverviewRequest request) {
        return facade.getRefundReasons(toPeriod(request), request.getTopLimit());
    }

    @GetMapping("/shipments/by-provider")
    public List<BreakdownStatisticResponse> getShipmentsByProvider(@Valid @ModelAttribute StatisticsOverviewRequest request) {
        return facade.getShipmentsByProvider(toPeriod(request));
    }

    @GetMapping("/shipments/failure-reasons")
    public List<BreakdownStatisticResponse> getShipmentFailureReasons(@Valid @ModelAttribute StatisticsOverviewRequest request) {
        return facade.getShipmentFailureReasons(toPeriod(request), request.getTopLimit());
    }

    private StatisticPeriod toPeriod(StatisticsOverviewRequest request) {
        return StatisticPeriod.of(
                request.getFrom(), request.getTo(), request.getTimezone(),
                request.getGroupBy(), request.isCompareWithPreviousPeriod()
        );
    }
}
