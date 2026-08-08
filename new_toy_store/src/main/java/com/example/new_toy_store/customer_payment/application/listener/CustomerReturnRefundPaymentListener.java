package com.example.new_toy_store.customer_payment.application.listener;

import com.example.new_toy_store.global.event.CustomerReturnRefundFinalizedEvent;
import com.example.new_toy_store.customer_payment.application.service.PaymentService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CustomerReturnRefundPaymentListener {

    private final PaymentService paymentService;

    public CustomerReturnRefundPaymentListener(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createRefundRequest(CustomerReturnRefundFinalizedEvent event) {
        paymentService.requestRefundForCustomerReturn(
                event.returnId(),
                event.orderId(),
                event.refundAmount(),
                event.reason()
        );
    }
}
