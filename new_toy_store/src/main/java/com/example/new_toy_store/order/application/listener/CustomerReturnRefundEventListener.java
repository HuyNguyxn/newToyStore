package com.example.new_toy_store.order.application.listener;

import com.example.new_toy_store.global.event.CustomerReturnRefundSucceededEvent;
import com.example.new_toy_store.order.application.facade.OrderFacade;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CustomerReturnRefundEventListener {

    private final OrderFacade orderFacade;

    public CustomerReturnRefundEventListener(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateOrderRefundState(CustomerReturnRefundSucceededEvent event) {
        orderFacade.updateOrderRefundStatus(event.orderId(), event.returnedOrderItemQuantities());
    }
}
