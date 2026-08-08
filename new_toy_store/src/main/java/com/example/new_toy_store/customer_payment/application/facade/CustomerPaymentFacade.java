package com.example.new_toy_store.customer_payment.application.facade;

import com.example.new_toy_store.customer_payment.application.service.PaymentService;
import com.example.new_toy_store.customer_payment.application.dto.response.CustomerPaymentResponse;
import org.springframework.stereotype.Component;

@Component
public class CustomerPaymentFacade {

    private final PaymentService paymentService;

    public CustomerPaymentFacade(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public CustomerPaymentResponse getPaymentDetails(Integer paymentId, Integer currentUserId, boolean isAdmin) {
        return paymentService.getDetails(paymentId, currentUserId, isAdmin);
    }

    public boolean hasSucceededPaymentForOrder(Integer orderId) {
        return paymentService.hasSucceededPaymentForOrder(orderId);
    }
}
