package com.example.new_toy_store.supplier_return.mapper;

import com.example.new_toy_store.supplier_return.application.dto.request.SupplierReturnItemRequest;
import com.example.new_toy_store.supplier_return.application.dto.request.SupplierReturnRequest;
import com.example.new_toy_store.supplier_return.application.dto.response.SupplierReturnItemResponse;
import com.example.new_toy_store.supplier_return.application.dto.response.SupplierReturnResponse;
import com.example.new_toy_store.supplier_return.domain.*;

import java.util.List;
import java.util.stream.Collectors;

public class SupplierReturnMapper {

    private SupplierReturnMapper() {
    }


    public static SupplierReturn mapRequestToNewEntity(SupplierReturnRequest request, String actionBy) {
        SupplierReturn entity = new SupplierReturn(
                request.getSupplierId(),
                request.getImportNoteId(),
                request.getFreightCost(),
                request.getRestockingFee(),
                request.getNote(),
                actionBy
        );

        if (request.getItems() != null) {
            request.getItems().forEach(itemReq ->
                    entity.addItem(mapItemRequestToEntity(itemReq))
            );
        }

        if (request.getImageUrls() != null) {
            request.getImageUrls().forEach(url ->
                    entity.addImage(new SupplierReturnImage(url))
            );
        }

        return entity;
    }

    private static SupplierReturnItem mapItemRequestToEntity(SupplierReturnItemRequest reqItem) {
        return new SupplierReturnItem(
                reqItem.getProductId(),
                reqItem.getVariantId(),
                reqItem.getProductName(),
                reqItem.getQuantity(),
                reqItem.getReturnPrice(),
                reqItem.getVatRate(),
                reqItem.getDiscountAmount(),
                SupplierReturnReason.from(reqItem.getReasonCode())
        );
    }

    public static SupplierReturnResponse mapEntityToResponse(SupplierReturn entity) {
        SupplierReturnResponse response = new SupplierReturnResponse();

        response.setId(entity.getId());
        response.setSupplierId(entity.getSupplierId());
        response.setImportNoteId(entity.getImportNoteId());
        response.setStatus(entity.getStatus().name());
        response.setStatusDisplayName(entity.getStatus().getDisplayName());
        response.setTotalRefundAmount(entity.getTotalRefundAmount());
        response.setFreightCost(entity.getFreightCost());
        response.setRestockingFee(entity.getRestockingFee());
        response.setNote(entity.getNote());

        response.setAvailableNextActions(entity.getStatus().getNextValidStateNames());

        if (entity.getItems() != null && !entity.getItems().isEmpty()) {
            response.setItems(mapItemsToResponseList(entity.getItems()));
        }

        return response;
    }

    private static List<SupplierReturnItemResponse> mapItemsToResponseList(List<SupplierReturnItem> items) {
        return items.stream()
                .map(SupplierReturnMapper::mapSingleItemToResponse)
                .collect(Collectors.toList());
    }

    private static SupplierReturnItemResponse mapSingleItemToResponse(SupplierReturnItem item) {
        SupplierReturnItemResponse res = new SupplierReturnItemResponse();

        res.setId(item.getId());
        res.setProductId(item.getProductId());
        res.setVariantId(item.getVariantId());
        res.setProductName(item.getProductName());
        res.setQuantity(item.getQuantity());
        res.setReturnPrice(item.getReturnPrice());
        res.setTaxAmount(item.getTaxAmount());
        res.setReasonCode(item.getReasonCode().name());
        res.setReasonDescription(item.getReasonCode().getDescription());

        return res;
    }
}