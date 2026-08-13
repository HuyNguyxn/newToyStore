package com.example.new_toy_store.infrastructure.schedule;

import com.example.new_toy_store.customer_payment.application.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class CustomerPaymentReconciliationScheduler {

    private static final Logger log = LoggerFactory.getLogger(CustomerPaymentReconciliationScheduler.class);

    private final PaymentService paymentService;

    public CustomerPaymentReconciliationScheduler(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reconcileAfterStartup() {
        reconcileSafely("startup");
    }

    @Scheduled(fixedDelayString = "${app.payment.reconciliation-delay-ms:300000}")
    public void reconcilePeriodically() {
        reconcileSafely("scheduled task");
    }

    private void reconcileSafely(String trigger) {
        try {
            int completedCodPayments = paymentService.reconcileCompletedCodPayments();
            int cancelledOrderPayments = paymentService.reconcileCancelledOrderPayments();
            if (completedCodPayments > 0 || cancelledOrderPayments > 0) {
                log.info(
                        "Reconciled {} completed COD payment(s) and {} cancelled-order payment(s) during {}",
                        completedCodPayments, cancelledOrderPayments, trigger
                );
            }
        } catch (RuntimeException exception) {
            log.error("Unable to reconcile customer payment statuses during {}", trigger, exception);
        }
    }
}
