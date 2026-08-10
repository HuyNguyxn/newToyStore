package com.example.new_toy_store.infrastructure.schedule;

import com.example.new_toy_store.customer_payment.application.service.PaymentService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CustomerPaymentReconciliationScheduler {

    private final PaymentService paymentService;

    public CustomerPaymentReconciliationScheduler(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reconcileAfterStartup() {
        paymentService.reconcileCompletedCodPayments();
    }

    @Scheduled(fixedDelayString = "${app.payment.reconciliation-delay-ms:300000}")
    public void reconcilePeriodically() {
        paymentService.reconcileCompletedCodPayments();
    }
}
