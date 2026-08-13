package com.example.new_toy_store.statistics.api;

import com.example.new_toy_store.statistics.application.facade.StatisticsFacade;
import com.example.new_toy_store.statistics.application.dto.request.StatisticsOverviewRequest;
import com.example.new_toy_store.statistics.application.dto.request.TopSellingProductsRequest;
import com.example.new_toy_store.statistics.application.dto.response.BreakdownStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.PaymentMethodStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.RevenueTrendPointResponse;
import com.example.new_toy_store.statistics.application.dto.response.StatisticsOverviewResponse;
import com.example.new_toy_store.statistics.application.dto.response.TopSellingProductResponse;
import com.example.new_toy_store.statistics.application.dto.response.TopSpendingCustomerResponse;
import com.example.new_toy_store.statistics.application.dto.response.InventoryMovementStatisticResponse;
import com.example.new_toy_store.statistics.application.dto.response.ProfitMarginStatisticResponse;
import com.example.new_toy_store.statistics.domain.StatisticPeriod;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/products/slow-selling")
    public List<TopSellingProductResponse> getSlowSellingProducts(
            @Valid @ModelAttribute TopSellingProductsRequest request,
            @RequestParam(defaultValue = "5") int maxUnits
    ) {
        StatisticPeriod period = StatisticPeriod.of(
                request.getFrom(), request.getTo(), request.getTimezone(), request.getGroupBy(), false
        );
        return facade.getSlowSellingProducts(period, request.getLimit(), maxUnits);
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

    @GetMapping("/customers/top-spending")
    public List<TopSpendingCustomerResponse> getTopSpendingCustomers(@Valid @ModelAttribute StatisticsOverviewRequest request) {
        return facade.getTopSpendingCustomers(toPeriod(request), request.getTopLimit());
    }

    @GetMapping("/inventory/snapshot")
    public List<InventoryMovementStatisticResponse> getInventorySnapshot(
            @RequestParam(defaultValue = "5") int lowStockThreshold
    ) {
        return facade.getInventorySnapshot(lowStockThreshold);
    }

    @GetMapping("/inventory/movements")
    public List<InventoryMovementStatisticResponse> getInventoryMovements(
            @Valid @ModelAttribute StatisticsOverviewRequest request
    ) {
        return facade.getInventoryMovements(toPeriod(request), request.getLowStockThreshold());
    }

    @GetMapping("/profit-margin")
    public List<ProfitMarginStatisticResponse> getProfitMargin(
            @Valid @ModelAttribute StatisticsOverviewRequest request
    ) {
        return facade.getProfitMargin(toPeriod(request), request.getTopLimit());
    }

    private StatisticPeriod toPeriod(StatisticsOverviewRequest request) {
        return StatisticPeriod.of(
                request.getFrom(), request.getTo(), request.getTimezone(),
                request.getGroupBy(), request.isCompareWithPreviousPeriod()
        );
    }
}
