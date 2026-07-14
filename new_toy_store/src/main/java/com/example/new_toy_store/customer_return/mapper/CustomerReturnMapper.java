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
        List<CustomerReturnItem> items = request.getItems().stream()
                .map(CustomerReturnMapper::toItemEntity)
                .collect(Collectors.toList());

        return new CustomerReturn(
                request.getOrderId(),
                items,
                request.getProofImageUrls(),
                customerUsername,
                request.getReasonNote()
        );
    }

    public static CustomerReturnResponse toResponse(CustomerReturn entity) {
        return buildResponse(entity);
    }

    private static CustomerReturnResponse buildResponse(CustomerReturn entity) {
        CustomerReturnResponse response = new CustomerReturnResponse(
                entity.getId(),
                entity.getOrderId(),
                roundDouble(entity.getReturnShippingFee()),
                roundDouble(entity.calculateRawTotalRefund()),
                entity.getStatus().name(),
                entity.getStatus().getDisplayName()
        );

        response.setItems(mapItems(entity.getItems()));
        response.setHistories(mapHistories(entity.getHistories()));
        response.setProofImages(mapImages(entity.getProofImages())); // Trích xuất ảnh trả về
        response.setAvailableActions(getAvailableActions(entity.getStatus()));

        return response;
    }

    private static CustomerReturnItem toItemEntity(CustomerReturnItemRequest req) {
        return new CustomerReturnItem(
                req.getOrderItemId(),
                req.getQuantity(),
                ReturnReasonCode.from(req.getReasonCode()),
                req.getExpectedRefundAmount()
        );
    }

    private static List<CustomerReturnItemResponse> mapItems(List<CustomerReturnItem> items) {
        if (items == null || items.isEmpty()) return Collections.emptyList();
        return items.stream().map(i -> new CustomerReturnItemResponse(
                i.getId(), i.getOrderItemId(), i.getQuantity(),
                i.getReasonCode().name(), i.getReasonCode().getDescription(),
                roundDouble(i.getExpectedRefundAmount())
        )).collect(Collectors.toList());
    }

    private static List<CustomerReturnHistoryResponse> mapHistories(List<CustomerReturnHistory> histories) {
        if (histories == null || histories.isEmpty()) return Collections.emptyList();
        return histories.stream().map(h -> new CustomerReturnHistoryResponse(
                h.getId(), h.getOldStatus() != null ? h.getOldStatus().name() : null,
                h.getNewStatus().name(), h.getActionBy(), h.getActionDate(), h.getNote()
        )).collect(Collectors.toList());
    }

    private static List<String> mapImages(List<CustomerReturnImage> images) {
        if (images == null || images.isEmpty()) return Collections.emptyList();
        return images.stream().map(CustomerReturnImage::getImageUrl).collect(Collectors.toList());
    }

    private static List<String> getAvailableActions(CustomerReturnStatus status) {
        if (status == null) return Collections.emptyList();
        return status.getNextValidStates().stream().map(Enum::name).collect(Collectors.toList());
    }

    private static double roundDouble(double value) {
        return Math.max(0.0, Math.round(value * 100.0) / 100.0);
    }
}