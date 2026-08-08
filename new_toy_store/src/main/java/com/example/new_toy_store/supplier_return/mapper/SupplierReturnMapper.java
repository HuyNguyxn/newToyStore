package com.example.new_toy_store.supplier_return.mapper;

import com.example.new_toy_store.supplier_return.application.dto.request.SupplierReturnItemRequest;
import com.example.new_toy_store.supplier_return.application.dto.request.SupplierReturnRequest;
import com.example.new_toy_store.supplier_return.application.dto.response.SupplierReturnActionResponse;
import com.example.new_toy_store.supplier_return.application.dto.response.SupplierReturnHistoryResponse;
import com.example.new_toy_store.supplier_return.application.dto.response.SupplierReturnImageResponse;
import com.example.new_toy_store.supplier_return.application.dto.response.SupplierReturnItemResponse;
import com.example.new_toy_store.supplier_return.application.dto.response.SupplierReturnResponse;
import com.example.new_toy_store.supplier_return.domain.SupplierReturn;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnHistory;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnImage;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnItem;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnReason;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnStatus;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SupplierReturnMapper {

    private SupplierReturnMapper() {
    }

    public static SupplierReturn mapRequestToNewEntity(SupplierReturnRequest request, String adminUsername) {
        SupplierReturn entity = createSupplierReturnEntity(request, adminUsername);
        mapItemEntities(request.getItems()).forEach(entity::addItem);
        return entity;
    }

    public static SupplierReturnResponse mapEntityToResponse(SupplierReturn entity) {
        if (entity == null) {
            return null;
        }
        return toDetailResponse(entity);
    }

    /**
     * Maps only fields needed by the paginated list. Keeping child collections
     * out of this projection prevents one query per return for items, history,
     * and images (N+1) while preserving the full detail endpoint.
     */
    public static SupplierReturnResponse toSummaryResponse(SupplierReturn entity) {
        if (entity == null) {
            return null;
        }
        SupplierReturnResponse response = createSupplierReturnResponse(entity);
        response.setItems(Collections.emptyList());
        response.setHistories(Collections.emptyList());
        response.setImages(Collections.emptyList());
        response.setAvailableActions(mapAvailableActions(entity.getStatus()));
        return response;
    }

    public static SupplierReturnResponse toDetailResponse(SupplierReturn entity) {
        SupplierReturnResponse response = createSupplierReturnResponse(entity);
        response.setItems(mapItemResponses(entity.getItems()));
        response.setHistories(mapHistoryResponses(entity.getHistories()));
        response.setImages(mapImageResponses(entity.getImages()));
        response.setAvailableActions(mapAvailableActions(entity.getStatus()));
        return response;
    }

    private static SupplierReturn createSupplierReturnEntity(SupplierReturnRequest request, String adminUsername) {
        return new SupplierReturn(
                request.getSupplierId(),
                request.getImportNoteId(),
                request.getFreightCost(),
                request.getRestockingFee(),
                request.getNote(),
                adminUsername
        );
    }

    private static SupplierReturnResponse createSupplierReturnResponse(SupplierReturn entity) {
        SupplierReturnResponse response = new SupplierReturnResponse();
        response.setId(entity.getId());
        response.setSupplierId(entity.getSupplierId());
        response.setImportNoteId(entity.getImportNoteId());
        response.setStatus(entity.getStatus());
        response.setFreightCost(entity.getFreightCost());
        response.setRestockingFee(entity.getRestockingFee());
        response.setTotalRefundAmount(entity.getTotalRefundAmount());
        response.setNote(entity.getNote());
        return response;
    }

    private static List<SupplierReturnItem> mapItemEntities(List<SupplierReturnItemRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }
        return requests.stream()
                .map(SupplierReturnMapper::mapItemEntity)
                .collect(Collectors.toList());
    }

    private static SupplierReturnItem mapItemEntity(SupplierReturnItemRequest request) {
        return new SupplierReturnItem(
                request.getProductId(),
                request.getVariantId(),
                request.getProductName(),
                request.getQuantity(),
                request.getReturnPrice(),
                request.getDiscountAmount(),
                SupplierReturnReason.from(request.getReasonCode()),
                request.getBatchNumber(),
                request.getExpiryDate()
        );
    }

    private static List<SupplierReturnItemResponse> mapItemResponses(List<SupplierReturnItem> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        return items.stream()
                .map(SupplierReturnMapper::mapItemResponse)
                .collect(Collectors.toList());
    }

    private static SupplierReturnItemResponse mapItemResponse(SupplierReturnItem item) {
        SupplierReturnItemResponse response = new SupplierReturnItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setVariantId(item.getVariantId());
        response.setProductName(item.getProductName());
        response.setQuantity(item.getQuantity());
        response.setAcceptedQuantity(item.getAcceptedQuantity());
        response.setReturnPrice(item.getReturnPrice());
        response.setDiscountAmount(item.getDiscountAmount());
        response.setReason(item.getReasonCode());
        response.setDiscrepancyReason(item.getDiscrepancyReason());
        response.setBatchNumber(item.getBatchNumber());
        response.setExpiryDate(item.getExpiryDate());
        return response;
    }

    private static List<SupplierReturnHistoryResponse> mapHistoryResponses(List<SupplierReturnHistory> histories) {
        if (histories == null || histories.isEmpty()) {
            return Collections.emptyList();
        }
        return histories.stream()
                .map(SupplierReturnMapper::mapHistoryResponse)
                .collect(Collectors.toList());
    }

    private static SupplierReturnHistoryResponse mapHistoryResponse(SupplierReturnHistory history) {
        SupplierReturnHistoryResponse response = new SupplierReturnHistoryResponse();
        response.setId(history.getId());
        response.setOldStatus(history.getOldStatus());
        response.setNewStatus(history.getNewStatus());
        response.setActionBy(history.getActionBy());
        response.setNote(history.getNote());
        response.setCreatedAt(history.getCreatedAt());
        return response;
    }

    private static List<SupplierReturnImageResponse> mapImageResponses(List<SupplierReturnImage> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        return images.stream()
                .map(SupplierReturnMapper::mapImageResponse)
                .collect(Collectors.toList());
    }

    private static SupplierReturnImageResponse mapImageResponse(SupplierReturnImage image) {
        SupplierReturnImageResponse response = new SupplierReturnImageResponse();
        response.setId(image.getId());
        response.setImageUrl(image.getImageUrl());
        response.setCreatedAt(image.getCreatedAt());
        return response;
    }

    private static List<SupplierReturnActionResponse> mapAvailableActions(SupplierReturnStatus status) {
        if (status == null) {
            return Collections.emptyList();
        }
        return status.getAllowedNextStatusCodes().stream()
                .map(SupplierReturnMapper::mapStatusAction)
                .toList();
    }

    private static SupplierReturnActionResponse mapStatusAction(String targetStatus) {
        return new SupplierReturnActionResponse(toActionCode(targetStatus), targetStatus, toActionLabel(targetStatus));
    }

    private static String toActionCode(String targetStatus) {
        return switch (targetStatus) {
            case "PENDING_APPROVAL" -> "SUBMIT";
            case "APPROVED" -> "APPROVE";
            case "REJECTED" -> "REJECT";
            case "SHIPPED" -> "SHIP";
            case "COMPLETED" -> "COMPLETE";
            case "CANCELLED" -> "CANCEL";
            default -> "CHANGE_STATUS";
        };
    }

    private static String toActionLabel(String targetStatus) {
        return switch (targetStatus) {
            case "PENDING_APPROVAL" -> "Trình duyệt";
            case "APPROVED" -> "Duyệt xuất trả";
            case "REJECTED" -> "Từ chối phiếu";
            case "SHIPPED" -> "Xuất kho";
            case "COMPLETED" -> "Hoàn tất";
            case "CANCELLED" -> "Hủy phiếu";
            default -> "Chuyển trạng thái";
        };
    }
}
