package com.example.new_toy_store.payment.mapper;

import com.example.new_toy_store.payment.application.dto.response.PaymentActionResponse;
import com.example.new_toy_store.payment.application.dto.response.PaymentRefundResponse;
import com.example.new_toy_store.payment.application.dto.response.PaymentResponse;
import com.example.new_toy_store.payment.domain.PaymentRefund;
import com.example.new_toy_store.payment.domain.PaymentStatus;
import com.example.new_toy_store.payment.domain.PaymentTransaction;

import java.util.List;

public final class PaymentMapper {

    private PaymentMapper() {}

    public static PaymentResponse toResponse(PaymentTransaction payment) {
        if (payment == null) return null;

        PaymentResponse response = new PaymentResponse();
        mapCoreFields(payment, response);
        mapStatusActions(payment, response);
        return response;
    }

    public static PaymentResponse toCheckoutResponse(PaymentTransaction payment, String paymentUrl, String gatewayMessage) {
        PaymentResponse response = toResponse(payment);
        if (response == null) return null;
        response.setPaymentUrl(paymentUrl);
        response.setGatewayMessage(gatewayMessage);
        return response;
    }

    public static PaymentRefundResponse toRefundResponse(PaymentRefund refund) {
        if (refund == null) return null;

        PaymentRefundResponse response = new PaymentRefundResponse();
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

    private static void mapCoreFields(PaymentTransaction payment, PaymentResponse response) {
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
    }

    private static void mapStatusActions(PaymentTransaction payment, PaymentResponse response) {
        List<PaymentStatus> nextStatuses = payment.getStatus().getNextValidStates();
        response.setAllowedNextStatuses(nextStatuses);
        response.setAvailableActions(nextStatuses.stream().map(PaymentStatus::name).toList());
        response.setNextActions(nextStatuses.stream().map(PaymentMapper::toActionResponse).toList());
    }

    private static PaymentActionResponse toActionResponse(PaymentStatus status) {
        return new PaymentActionResponse(status.name(), status.getDisplayName(), status.getDescription());
    }
}
