package com.example.new_toy_store.supplier_return.mapper;

import com.example.new_toy_store.supplier_return.application.dto.request.SupplierReturnRequest;
import com.example.new_toy_store.supplier_return.application.dto.request.SupplierReturnItemRequest;
import com.example.new_toy_store.supplier_return.application.dto.response.SupplierReturnResponse;
import com.example.new_toy_store.supplier_return.application.dto.response.SupplierReturnItemResponse;
import com.example.new_toy_store.supplier_return.domain.SupplierReturn;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnItem;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnReason;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SupplierReturnMapper {

    private SupplierReturnMapper() {
    }

    public static SupplierReturn mapRequestToNewEntity(SupplierReturnRequest request, String adminUsername) {
        SupplierReturn entity = new SupplierReturn(
                request.getSupplierId(),
                request.getImportNoteId(),
                request.getFreightCost(),
                request.getRestockingFee(),
                request.getNote(),
                adminUsername
        );

        if (request.getItems() != null) {
            for (SupplierReturnItemRequest itemReq : request.getItems()) {
                SupplierReturnItem item = new SupplierReturnItem(
                        itemReq.getProductId(),
                        itemReq.getVariantId(),
                        itemReq.getProductName(),
                        itemReq.getQuantity(),
                        itemReq.getReturnPrice(),
                        itemReq.getDiscountAmount(),
                        SupplierReturnReason.from(itemReq.getReasonCode()),
                        itemReq.getBatchNumber(),
                        itemReq.getExpiryDate()
                );
                entity.addItem(item);
            }
        }
        return entity;
    }

    public static SupplierReturnResponse mapEntityToResponse(SupplierReturn entity) {
        if (entity == null) {
            return null;
        }

        SupplierReturnResponse res = new SupplierReturnResponse();
        res.setId(entity.getId());
        res.setSupplierId(entity.getSupplierId());
        res.setImportNoteId(entity.getImportNoteId());
        res.setStatus(entity.getStatus().name());
        res.setStatusDisplayName(entity.getStatus().getDisplayName());
        res.setFreightCost(entity.getFreightCost());
        res.setRestockingFee(entity.getRestockingFee());
        res.setTotalRefundAmount(entity.getTotalRefundAmount());
        res.setNote(entity.getNote());

        List<SupplierReturnItemResponse> itemResponses = entity.getItems().stream()
                .map(item -> {
                    SupplierReturnItemResponse itemRes = new SupplierReturnItemResponse();
                    itemRes.setId(item.getId());
                    itemRes.setProductId(item.getProductId());
                    itemRes.setVariantId(item.getVariantId());
                    itemRes.setProductName(item.getProductName());
                    itemRes.setQuantity(item.getQuantity());
                    itemRes.setAcceptedQuantity(item.getAcceptedQuantity());
                    itemRes.setReturnPrice(item.getReturnPrice());
                    itemRes.setDiscountAmount(item.getDiscountAmount());
                    itemRes.setReasonCode(item.getReasonCode().name());
                    itemRes.setReasonDescription(item.getReasonCode().getDescription());
                    itemRes.setDiscrepancyReason(item.getDiscrepancyReason());
                    itemRes.setBatchNumber(item.getBatchNumber());
                    itemRes.setExpiryDate(item.getExpiryDate());
                    return itemRes;
                })
                .collect(Collectors.toList());

        res.setItems(itemResponses);
        res.setAvailableNextActions(entity.getStatus().getNextValidStateNames());
        res.setHistories(new ArrayList<>());
        res.setImages(new ArrayList<>());

        return res;
    }
}