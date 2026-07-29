package com.example.new_toy_store.product.application.listener;

import com.example.new_toy_store.global.event.CustomerReturnStockRestorationRequestedEvent;
import com.example.new_toy_store.product.application.facade.ProductFacade;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CustomerReturnStockEventListener {

    private final ProductFacade productFacade;

    public CustomerReturnStockEventListener(ProductFacade productFacade) {
        this.productFacade = productFacade;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void restoreSellableStock(CustomerReturnStockRestorationRequestedEvent event) {
        productFacade.restoreStockForCancelledOrder(event.variantQuantities());
    }
}
