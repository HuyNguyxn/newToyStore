package com.example.new_toy_store.customer_payment.mapper;

import com.example.new_toy_store.customer_payment.application.dto.response.CustomerPaymentActionResponse;
import com.example.new_toy_store.customer_payment.application.dto.response.CustomerPaymentRefundResponse;
import com.example.new_toy_store.customer_payment.application.dto.response.CustomerPaymentResponse;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentRefund;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentStatus;
import com.example.new_toy_store.customer_payment.domain.CustomerPaymentTransaction;

import java.util.List;

public final class CustomerPaymentMapper {

    private CustomerPaymentMapper() {}

    public static CustomerPaymentResponse toResponse(CustomerPaymentTransaction payment) {
        if (payment == null) return null;

        CustomerPaymentResponse response = new CustomerPaymentResponse();
        mapCoreFields(payment, response);
        mapStatusActions(payment, response);
        return response;
    }

    public static CustomerPaymentResponse toCheckoutResponse(CustomerPaymentTransaction payment, String paymentUrl, String gatewayMessage) {
        CustomerPaymentResponse response = toResponse(payment);
        if (response == null) return null;
        response.setPaymentUrl(paymentUrl);
        response.setGatewayMessage(gatewayMessage);
        return response;
    }

    public static CustomerPaymentRefundResponse toRefundResponse(CustomerPaymentRefund refund) {
        if (refund == null) return null;

        CustomerPaymentRefundResponse response = new CustomerPaymentRefundResponse();
        response.setId(refund.getId());
        response.setPaymentId(refund.getPaymentId());
        response.setOrderId(refund.getOrderId());
        response.setUserId(refund.getUserId());
        response.setRefundCode(refund.getRefundCode());
        response.setMethod(refund.getMethod());
        response.setStatus(refund.getStatus());
        response.setAmount(refund.getAmount());
        response.setReason(refund.getReason());
        response.setProviderRefundId(refund.getProviderRefundId());
        response.setFailedReason(refund.getFailedReason());
        response.setProcessedAt(refund.getProcessedAt());
        response.setCompletedAt(refund.getCompletedAt());
        response.setCreatedAt(refund.getCreatedAt());
        response.setUpdatedAt(refund.getUpdatedAt());
        response.setAllowedNextStatuses(refund.getStatus().getNextValidStates());
        return response;
    }

    private static void mapCoreFields(CustomerPaymentTransaction payment, CustomerPaymentResponse response) {
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setUserId(payment.getUserId());
        response.setMethod(payment.getMethod());
        response.setStatus(payment.getStatus());
        response.setAmount(payment.getAmount());
        response.setProviderTransactionId(payment.getProviderTransactionId());
        response.setFailureReason(payment.getFailureReason());
        response.setCancelReason(payment.getCancelReason());
        response.setPaidAt(payment.getPaidAt());
        response.setExpiredAt(payment.getExpiredAt());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        response.setIdempotencyKey(payment.getIdempotencyKey());
    }

    private static void mapStatusActions(CustomerPaymentTransaction payment, CustomerPaymentResponse response) {
        List<CustomerPaymentStatus> nextStatuses = payment.getStatus().getNextValidStates();
        response.setAllowedNextStatuses(nextStatuses);
        response.setAvailableActions(nextStatuses.stream().map(CustomerPaymentStatus::name).toList());
        response.setNextActions(nextStatuses.stream().map(CustomerPaymentMapper::toActionResponse).toList());
    }

    private static CustomerPaymentActionResponse toActionResponse(CustomerPaymentStatus status) {
        return new CustomerPaymentActionResponse(status.name(), status.getDisplayName(), status.getDescription());
    }
}
