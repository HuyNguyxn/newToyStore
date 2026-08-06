package com.example.new_toy_store.logistics.mapper;

import com.example.new_toy_store.logistics.application.dto.response.ShipmentActionResponse;
import com.example.new_toy_store.logistics.application.dto.response.ShipmentItemResponse;
import com.example.new_toy_store.logistics.application.dto.response.ShipmentResponse;
import com.example.new_toy_store.logistics.application.dto.response.ShipmentTrackingLogResponse;
import com.example.new_toy_store.logistics.domain.Shipment;
import com.example.new_toy_store.logistics.domain.ShipmentAction;
import com.example.new_toy_store.logistics.domain.ShipmentItem;
import com.example.new_toy_store.logistics.domain.ShipmentTrackingLog;

import java.util.List;

public final class ShipmentMapper {

    private ShipmentMapper() {}

    public static ShipmentResponse toResponse(Shipment shipment) {
        if (shipment == null) return null;

        ShipmentResponse response = new ShipmentResponse();
        mapCoreFields(shipment, response);
        mapItems(shipment, response);
        mapStatusActions(shipment, response);
        return response;
    }

    public static ShipmentTrackingLogResponse toTrackingLogResponse(ShipmentTrackingLog log) {
        if (log == null) return null;

        ShipmentTrackingLogResponse response = new ShipmentTrackingLogResponse();
        response.setId(log.getId());
        response.setShipmentId(log.getShipmentId());
        response.setStatus(log.getStatus());
        response.setLocation(log.getLocation());
        response.setDescription(log.getDescription());
        response.setOccurredAt(log.getOccurredAt());
        return response;
    }

    private static void mapCoreFields(Shipment shipment, ShipmentResponse response) {
        response.setId(shipment.getId());
        response.setTrackingCode(shipment.getTrackingCode());
        response.setOrderId(shipment.getOrderId());
        response.setUserId(shipment.getUserId());
        response.setProviderCode(shipment.getProviderCode());
        response.setProviderShipmentCode(shipment.getProviderShipmentCode());
        response.setRecipientName(shipment.getRecipientName());
        response.setRecipientPhone(shipment.getRecipientPhone());
        response.setShippingAddressSnapshot(shipment.getShippingAddressSnapshot());
        response.setShippingFee(shipment.getShippingFee());
        response.setCodAmount(shipment.getCodAmount());
        response.setStatus(shipment.getStatus());
        response.setShipmentType(shipment.getShipmentType());
        response.setCustomerReturnId(shipment.getCustomerReturnId());
        response.setSupplierReturnId(shipment.getSupplierReturnId());
        response.setShipmentTypeDisplayName(shipment.getShipmentType() != null ? shipment.getShipmentType().getDisplayName() : null);
        response.setProviderDisplayName(shipment.getProviderCode() != null ? shipment.getProviderCode().getDisplayName() : null);
        response.setDeliveryAttemptCount(shipment.getDeliveryAttemptCount());
        response.setFailureReason(shipment.getFailureReason());
        response.setDeliveredAt(shipment.getDeliveredAt());
        response.setReturnedAt(shipment.getReturnedAt());
        response.setCancelledAt(shipment.getCancelledAt());
        response.setCreatedAt(shipment.getCreatedAt());
        response.setUpdatedAt(shipment.getUpdatedAt());
    }

    private static void mapItems(Shipment shipment, ShipmentResponse response) {
        response.setItems(shipment.getItems().stream().map(ShipmentMapper::toItemResponse).toList());
    }

    private static void mapStatusActions(Shipment shipment, ShipmentResponse response) {
        List<ShipmentAction> actions = shipment.getStatus().getAvailableActions();
        response.setAllowedActions(actions);
        response.setAvailableActions(actions.stream().map(ShipmentAction::name).toList());
        response.setAllowedNextStatuses(shipment.getStatus().getNextValidStates());
        response.setNextActions(actions.stream().map(ShipmentMapper::toActionResponse).toList());
    }

    private static ShipmentItemResponse toItemResponse(ShipmentItem item) {
        ShipmentItemResponse response = new ShipmentItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setVariantId(item.getVariantId());
        response.setProductNameSnapshot(item.getProductNameSnapshot());
        response.setVariantSnapshot(item.getVariantSnapshot());
        response.setQuantity(item.getQuantity());
        return response;
    }

    private static ShipmentActionResponse toActionResponse(ShipmentAction action) {
        return new ShipmentActionResponse(action.name(), action.getDisplayName(), action.getDescription());
    }

}
