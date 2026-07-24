package com.example.new_toy_store.customer_return.mapper;

import com.example.new_toy_store.customer_return.application.dto.request.CustomerReturnItemRequest;
import com.example.new_toy_store.customer_return.application.dto.request.CustomerReturnRequest;
import com.example.new_toy_store.customer_return.application.dto.response.CustomerReturnActionResponse;
import com.example.new_toy_store.customer_return.application.dto.response.CustomerReturnHistoryResponse;
import com.example.new_toy_store.customer_return.application.dto.response.CustomerReturnItemResponse;
import com.example.new_toy_store.customer_return.application.dto.response.CustomerReturnResponse;
import com.example.new_toy_store.customer_return.domain.CustomerReturn;
import com.example.new_toy_store.customer_return.domain.CustomerReturnHistory;
import com.example.new_toy_store.customer_return.domain.CustomerReturnImage;
import com.example.new_toy_store.customer_return.domain.CustomerReturnItem;
import com.example.new_toy_store.customer_return.domain.CustomerReturnStatus;
import com.example.new_toy_store.customer_return.domain.ReturnReasonCode;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerReturnMapper {

    private CustomerReturnMapper() {
    }

    public static CustomerReturn toEntity(CustomerReturnRequest request, String customerUsername) {
        return createCustomerReturnEntity(request, customerUsername);
    }

    public static CustomerReturnResponse toResponse(CustomerReturn entity) {
        return toDetailResponse(entity);
    }

    public static CustomerReturnResponse toDetailResponse(CustomerReturn entity) {
        CustomerReturnResponse response = createCustomerReturnResponse(entity);
        response.setItems(mapItemResponses(entity.getItems()));
        response.setHistories(mapHistoryResponses(entity.getHistories()));
        response.setProofImages(mapImageUrls(entity.getProofImages()));
        response.setAvailableActions(mapAvailableActions(entity.getStatus()));
        return response;
    }

    private static CustomerReturn createCustomerReturnEntity(CustomerReturnRequest request, String customerUsername) {
        return new CustomerReturn(
                request.getOrderId(),
                mapItemEntities(request.getItems()),
                request.getProofImageUrls(),
                customerUsername,
                request.getReasonNote()
        );
    }

    private static CustomerReturnResponse createCustomerReturnResponse(CustomerReturn entity) {
        return new CustomerReturnResponse(
                entity.getId(),
                entity.getOrderId(),
                roundToTwoDecimals(entity.getReturnShippingFee()),
                roundToTwoDecimals(entity.calculateRawTotalRefund()),
                entity.getStatus()
        );
    }

    private static List<CustomerReturnItem> mapItemEntities(List<CustomerReturnItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }
        return requests.stream()
                .map(CustomerReturnMapper::mapItemEntity)
                .collect(Collectors.toList());
    }

    private static CustomerReturnItem mapItemEntity(CustomerReturnItemRequest request) {
        return new CustomerReturnItem(
                request.getOrderItemId(),
                request.getProductId(),
                request.getVariantId(),
                request.getQuantity(),
                ReturnReasonCode.from(request.getReasonCode()),
                request.getExpectedRefundAmount()
        );
    }

    private static List<CustomerReturnItemResponse> mapItemResponses(List<CustomerReturnItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(CustomerReturnMapper::mapItemResponse)
                .collect(Collectors.toList());
    }

    private static CustomerReturnItemResponse mapItemResponse(CustomerReturnItem item) {
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

    private static List<CustomerReturnHistoryResponse> mapHistoryResponses(List<CustomerReturnHistory> histories) {
        if (histories == null || histories.isEmpty()) {
            return Collections.emptyList();
        }
        return histories.stream()
                .map(CustomerReturnMapper::mapHistoryResponse)
                .collect(Collectors.toList());
    }

    private static CustomerReturnHistoryResponse mapHistoryResponse(CustomerReturnHistory history) {
        return new CustomerReturnHistoryResponse(
                history.getId(),
                history.getOldStatus(),
                history.getNewStatus(),
                history.getActionBy(),
                history.getActionDate(),
                history.getNote()
        );
    }

    private static List<String> mapImageUrls(List<CustomerReturnImage> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        return images.stream()
                .map(CustomerReturnImage::getImageUrl)
                .collect(Collectors.toList());
    }

    private static List<CustomerReturnActionResponse> mapAvailableActions(CustomerReturnStatus status) {
        if (status == null) {
            return Collections.emptyList();
        }
        return status.getAllowedNextStatusCodes().stream()
                .map(CustomerReturnMapper::mapStatusAction)
                .toList();
    }

    private static CustomerReturnActionResponse mapStatusAction(String targetStatus) {
        return new CustomerReturnActionResponse(toActionCode(targetStatus), targetStatus, toActionLabel(targetStatus));
    }

    private static String toActionCode(String targetStatus) {
        return switch (targetStatus) {
            case "APPROVED" -> "APPROVE";
            case "REJECTED" -> "REJECT";
            case "NEEDS_MORE_INFO" -> "REQUEST_MORE_INFO";
            case "CANCELLED" -> "CANCEL";
            case "RECEIVED" -> "RECEIVE_ITEMS";
            case "INSPECTED_OK" -> "PASS_QC";
            case "INSPECTED_FAILED" -> "FAIL_QC";
            case "DISPUTED" -> "OPEN_DISPUTE";
            case "REFUNDED" -> "FINALIZE_REFUND";
            case "REPLACED" -> "FINALIZE_REPLACEMENT";
            case "REQUESTED" -> "RESUBMIT_INFO";
            default -> "CHANGE_STATUS";
        };
    }

    private static String toActionLabel(String targetStatus) {
        return switch (targetStatus) {
            case "APPROVED" -> "Duyệt yêu cầu";
            case "REJECTED" -> "Từ chối yêu cầu";
            case "NEEDS_MORE_INFO" -> "Yêu cầu bổ sung thông tin";
            case "CANCELLED" -> "Hủy yêu cầu";
            case "RECEIVED" -> "Xác nhận đã nhận hàng";
            case "INSPECTED_OK" -> "QC đạt";
            case "INSPECTED_FAILED" -> "QC không đạt";
            case "DISPUTED" -> "Mở tranh chấp";
            case "REFUNDED" -> "Hoàn tất hoàn tiền";
            case "REPLACED" -> "Hoàn tất đổi hàng";
            case "REQUESTED" -> "Gửi lại thông tin";
            default -> "Chuyển trạng thái";
        };
    }

    private static double roundToTwoDecimals(double value) {
        return Math.max(0.0, Math.round(value * 100.0) / 100.0);
    }
}
