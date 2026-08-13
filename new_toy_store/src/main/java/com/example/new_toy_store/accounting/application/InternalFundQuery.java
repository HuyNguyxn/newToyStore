package com.example.new_toy_store.accounting.application;

import com.example.new_toy_store.accounting.domain.JournalEntryLineRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class InternalFundQuery {
    private final JournalEntryLineRepository lineRepository;

    public InternalFundQuery(JournalEntryLineRepository lineRepository) {
        this.lineRepository = lineRepository;
    }

    @Transactional(readOnly = true)
    public double getAvailableFunds(LocalDate asOf) {
        double value = lineRepository.calculateLiquidFundsAsOf(asOf == null ? LocalDate.now() : asOf);
        return Math.round(value * 100.0) / 100.0;
    }
}
