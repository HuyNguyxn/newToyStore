package com.example.new_toy_store.customer_return.application.listener;

import com.example.new_toy_store.customer_return.application.service.CustomerReturnService;
import com.example.new_toy_store.global.event.PaymentRefundedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CustomerReturnPaymentRefundedListener {

    private final CustomerReturnService customerReturnService;

    public CustomerReturnPaymentRefundedListener(CustomerReturnService customerReturnService) {
        this.customerReturnService = customerReturnService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void confirmCustomerReturnRefund(PaymentRefundedEvent event) {
        if (event.customerReturnId() == null) return;
        customerReturnService.confirmRefundSucceeded(
                event.customerReturnId(),
                "SYSTEM_PAYMENT",
                "Hoàn tiền thành công theo mã " + event.refundCode()
        );
    }
}
