package com.example.new_toy_store.customer_return.application.facade;

import com.example.new_toy_store.customer_return.application.dto.response.CustomerReturnResponse;
import com.example.new_toy_store.customer_return.application.service.CustomerReturnService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class CustomerReturnFacade {

    private final CustomerReturnService customerReturnService;

    public CustomerReturnFacade(CustomerReturnService customerReturnService) {
        this.customerReturnService = customerReturnService;
    }

    public Page<CustomerReturnResponse> filterReturns(String status, Integer orderId, Pageable pageable) {
        return customerReturnService.filterReturns(status, orderId, pageable);
    }
}
