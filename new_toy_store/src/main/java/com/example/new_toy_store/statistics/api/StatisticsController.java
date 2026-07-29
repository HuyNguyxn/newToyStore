package com.example.new_toy_store.statistics.api;

import com.example.new_toy_store.statistics.application.facade.StatisticsFacade;
import com.example.new_toy_store.statistics.application.dto.request.StatisticsOverviewRequest;
import com.example.new_toy_store.statistics.application.dto.request.TopSellingProductsRequest;
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
        return facade.getOverview(period, request.getTopLimit(), request.getLowStockThreshold());
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
}
