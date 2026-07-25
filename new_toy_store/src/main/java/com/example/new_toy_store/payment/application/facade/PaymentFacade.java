package com.example.new_toy_store.payment.application.facade;

import com.example.new_toy_store.payment.application.PaymentService;
import com.example.new_toy_store.payment.application.dto.response.PaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentFacade {

    private final PaymentService paymentService;

    public PaymentFacade(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public PaymentResponse getPaymentDetails(Integer paymentId, Integer currentUserId, boolean isAdmin) {
        return paymentService.getDetails(paymentId, currentUserId, isAdmin);
    }

    public boolean hasSucceededPaymentForOrder(Integer orderId) {
        return paymentService.hasSucceededPaymentForOrder(orderId);
    }
}
