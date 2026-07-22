package com.example.new_toy_store.customer_return.mapper;

import com.example.new_toy_store.customer_return.application.dto.request.CustomerReturnItemRequest;
import com.example.new_toy_store.customer_return.application.dto.request.CustomerReturnRequest;
import com.example.new_toy_store.customer_return.application.dto.response.*;
import com.example.new_toy_store.customer_return.domain.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerReturnMapper {

    public static CustomerReturn toEntity(CustomerReturnRequest request, String customerUsername) {
        return buildCustomerReturnEntity(request, customerUsername);
    }

    public static CustomerReturnResponse toResponse(CustomerReturn entity) {
        return buildCustomerReturnResponse(entity);
    }

    private static CustomerReturn buildCustomerReturnEntity(CustomerReturnRequest request, String customerUsername) {
        List<CustomerReturnItem> items = extractItemsFromRequest(request.getItems());

        return new CustomerReturn(
                request.getOrderId(),
                items,
                request.getProofImageUrls(),
                customerUsername,
                request.getReasonNote()
        );
    }

    private static CustomerReturnResponse buildCustomerReturnResponse(CustomerReturn entity) {
        CustomerReturnResponse response = new CustomerReturnResponse(
                entity.getId(),
                entity.getOrderId(),
                roundToTwoDecimals(entity.getReturnShippingFee()),
                roundToTwoDecimals(entity.calculateRawTotalRefund()),
                entity.getStatus()
        );

        response.setItems(extractItemResponses(entity.getItems()));
        response.setHistories(extractHistoryResponses(entity.getHistories()));
        response.setProofImages(extractImageUrls(entity.getProofImages()));
        response.setAvailableActions(entity.getStatus().getNextValidStates());

        return response;
    }

    private static List<CustomerReturnItem> extractItemsFromRequest(List<CustomerReturnItemRequest> requests) {
        if (requests == null || requests.isEmpty()) return Collections.emptyList();
        return requests.stream()
                .map(CustomerReturnMapper::buildItemEntity)
                .collect(Collectors.toList());
    }

    private static List<CustomerReturnItemResponse> extractItemResponses(List<CustomerReturnItem> items) {
        if (items == null || items.isEmpty()) return Collections.emptyList();
        return items.stream()
                .map(CustomerReturnMapper::buildItemResponse)
                .collect(Collectors.toList());
    }

    private static List<CustomerReturnHistoryResponse> extractHistoryResponses(List<CustomerReturnHistory> histories) {
        if (histories == null || histories.isEmpty()) return Collections.emptyList();
        return histories.stream()
                .map(CustomerReturnMapper::buildHistoryResponse)
                .collect(Collectors.toList());
    }

    private static List<String> extractImageUrls(List<CustomerReturnImage> images) {
        if (images == null || images.isEmpty()) return Collections.emptyList();
        return images.stream()
                .map(CustomerReturnImage::getImageUrl)
                .collect(Collectors.toList());
    }
    private static CustomerReturnItem buildItemEntity(CustomerReturnItemRequest req) {
        return new CustomerReturnItem(
                req.getOrderItemId(),
                req.getProductId(),
                req.getVariantId(),
                req.getQuantity(),
                ReturnReasonCode.from(req.getReasonCode()),
                req.getExpectedRefundAmount()
        );
    }

    private static CustomerReturnItemResponse buildItemResponse(CustomerReturnItem item) {
        CustomerReturnItemResponse response = new CustomerReturnItemResponse(
                item.getId(),
                item.getOrderItemId(),
                item.getQuantity(),
                item.getReasonCode(),
                roundToTwoDecimals(item.getExpectedRefundAmount())
        );
        response.setProductId(item.getProductId());
        response.setVariantId(item.getVariantId());
        return response;
    }

    private static CustomerReturnHistoryResponse buildHistoryResponse(CustomerReturnHistory history) {
        return new CustomerReturnHistoryResponse(
                history.getId(),
                history.getOldStatus(),
                history.getNewStatus(),
                history.getActionBy(),
                history.getActionDate(),
                history.getNote()
        );
    }

    private static double roundToTwoDecimals(double value) {
        return Math.max(0.0, Math.round(value * 100.0) / 100.0);
    }
}