package com.example.new_toy_store.logistics.application;

import com.example.new_toy_store.global.event.ShipmentCancelledEvent;
import com.example.new_toy_store.global.event.ShipmentCreatedEvent;
import com.example.new_toy_store.global.event.ShipmentDeliveredEvent;
import com.example.new_toy_store.global.event.ShipmentInTransitEvent;
import com.example.new_toy_store.global.event.ShipmentReturnedEvent;
import com.example.new_toy_store.infrastructure.specification.ShipmentSpecification;
import com.example.new_toy_store.logistics.application.dto.request.ShipmentActionRequest;
import com.example.new_toy_store.logistics.application.dto.request.ShipmentFilterRequest;
import com.example.new_toy_store.logistics.application.dto.response.ShipmentResponse;
import com.example.new_toy_store.logistics.application.dto.response.ShipmentTrackingLogResponse;
import com.example.new_toy_store.logistics.domain.Shipment;
import com.example.new_toy_store.logistics.domain.ShipmentAction;
import com.example.new_toy_store.logistics.domain.ShipmentType;
import com.example.new_toy_store.logistics.domain.ShipmentRepository;
import com.example.new_toy_store.logistics.domain.ShipmentStatus;
import com.example.new_toy_store.logistics.domain.ShipmentTrackingLog;
import com.example.new_toy_store.logistics.domain.ShipmentTrackingLogRepository;
import com.example.new_toy_store.logistics.domain.exception.DuplicateShipmentException;
import com.example.new_toy_store.logistics.domain.exception.InvalidShipmentOperationException;
import com.example.new_toy_store.logistics.domain.exception.LogisticsCrossModuleException;
import com.example.new_toy_store.logistics.domain.exception.ShipmentAccessDeniedException;
import com.example.new_toy_store.logistics.domain.exception.ShipmentDeletedConflictException;
import com.example.new_toy_store.logistics.domain.exception.ShipmentNotFoundException;
import com.example.new_toy_store.logistics.mapper.ShipmentMapper;
import com.example.new_toy_store.order.application.dto.response.OrderLogisticsSnapshot;
import com.example.new_toy_store.order.application.facade.OrderFacade;
import com.example.new_toy_store.order.domain.OrderStatus;
import com.example.new_toy_store.payment.application.facade.PaymentFacade;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LogisticsService {

    private static final DateTimeFormatter TRACKING_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final ShipmentRepository shipmentRepository;
    private final ShipmentTrackingLogRepository trackingLogRepository;
    private final OrderFacade orderFacade;
    private final PaymentFacade paymentFacade;
    private final ApplicationEventPublisher eventPublisher;

    public LogisticsService(
            ShipmentRepository shipmentRepository,
            ShipmentTrackingLogRepository trackingLogRepository,
            OrderFacade orderFacade,
            PaymentFacade paymentFacade,
            ApplicationEventPublisher eventPublisher
    ) {
        this.shipmentRepository = shipmentRepository;
        this.trackingLogRepository = trackingLogRepository;
        this.orderFacade = orderFacade;
        this.paymentFacade = paymentFacade;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ShipmentResponse createForConfirmedOrder(Integer orderId) {
        if (shipmentRepository.existsByOrderId(orderId)) {
            return shipmentRepository.findByOrderId(orderId).map(ShipmentMapper::toResponse)
                    .orElseThrow(() -> new DuplicateShipmentException(orderId));
        }

        OrderLogisticsSnapshot order = orderFacade.getLogisticsSnapshot(orderId);
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw LogisticsCrossModuleException.invalidOrder(orderId, "only CONFIRMED orders can create shipments");
        }

        double codAmount = paymentFacade.hasSucceededPaymentForOrder(orderId) ? 0.0 : order.getTotalAmount();
        Shipment shipment = new Shipment(
                generateTrackingCode(orderId),
                order.getOrderId(),
                order.getUserId(),
                "Customer #" + order.getUserId(),
                null,
                order.getShippingAddress(),
                0.0,
                codAmount
        );

        order.getItems().forEach(item -> shipment.addItem(
                item.getProductId(),
                item.getVariantId(),
                item.getProductName(),
                item.getVariantSnapshot(),
                item.getQuantity()
        ));

        shipmentRepository.save(shipment);
        addTrackingLog(shipment, "Warehouse", "Shipment created and waiting for pickup");
        eventPublisher.publishEvent(ShipmentCreatedEvent.now(
                shipment.getId(),
                shipment.getOrderId(),
                shipment.getUserId(),
                shipment.getTrackingCode(),
                shipment.getCodAmount()
        ));
        return ShipmentMapper.toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse createForCustomerReturn(
            Integer returnId,
            Integer userId,
            String recipientName,
            String recipientPhone,
            String shippingAddressSnapshot,
            double shippingFee
    ) {
        if (shipmentRepository.existsByCustomerReturnId(returnId)) {
            return shipmentRepository.findByCustomerReturnId(returnId).map(ShipmentMapper::toResponse)
                    .orElseThrow(() -> new DuplicateShipmentException(returnId));
        }

        String trackingCode = "RET-" + LocalDateTime.now().format(TRACKING_DATE_FORMAT) + "-" + returnId;
        Shipment shipment = Shipment.createCustomerReturnShipment(
                trackingCode,
                returnId,
                userId,
                recipientName,
                recipientPhone,
                shippingAddressSnapshot,
                shippingFee
        );

        shipmentRepository.save(shipment);
        addTrackingLog(shipment, "Customer Address", "Return shipment request registered. Waiting for carrier pickup.");
        
        eventPublisher.publishEvent(ShipmentCreatedEvent.now(
                shipment.getId(),
                null, // No orderId for return
                shipment.getUserId(),
                shipment.getTrackingCode(),
                0.0
        ));
        
        return ShipmentMapper.toResponse(shipment);
    }

    @Transactional
    public ShipmentResponse createForSupplierReturn(
            Integer supplierReturnId,
            Integer userId,
            String recipientName,
            String recipientPhone,
            String shippingAddressSnapshot,
            double shippingFee
    ) {
        if (shipmentRepository.existsBySupplierReturnId(supplierReturnId)) {
            return shipmentRepository.findBySupplierReturnId(supplierReturnId).map(ShipmentMapper::toResponse)
                    .orElseThrow(() -> new DuplicateShipmentException(supplierReturnId));
        }

        String trackingCode = "SUP-" + LocalDateTime.now().format(TRACKING_DATE_FORMAT) + "-" + supplierReturnId;
        Shipment shipment = Shipment.createSupplierReturnShipment(
                trackingCode,
                supplierReturnId,
                userId,
                recipientName,
                recipientPhone,
                shippingAddressSnapshot,
                shippingFee
        );

        shipmentRepository.save(shipment);
        addTrackingLog(shipment, "Warehouse", "Supplier return shipment created. Waiting for carrier dispatch.");
        
        eventPublisher.publishEvent(ShipmentCreatedEvent.now(
                shipment.getId(),
                null,
                shipment.getUserId(),
                shipment.getTrackingCode(),
                0.0
        ));
        
        return ShipmentMapper.toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getDetails(Integer shipmentId, Integer currentUserId, boolean isAdminOrStaff) {
        Shipment shipment = getShipment(shipmentId);
        validateOwnership(shipment, currentUserId, isAdminOrStaff, "view");
        return ShipmentMapper.toResponse(shipment);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentResponse> getMyShipments(Integer userId, Pageable pageable) {
        return shipmentRepository.findByUserId(userId, pageable).map(ShipmentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentResponse> filter(ShipmentFilterRequest request, Pageable pageable) {
        Specification<Shipment> spec = ShipmentSpecification.filter(request);
        return shipmentRepository.findAll(spec, pageable).map(ShipmentMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ShipmentTrackingLogResponse> getTrackingLogs(
            Integer shipmentId,
            Integer currentUserId,
            boolean isAdminOrStaff,
            Pageable pageable
    ) {
        Shipment shipment = getShipment(shipmentId);
        validateOwnership(shipment, currentUserId, isAdminOrStaff, "view tracking logs");
        return trackingLogRepository.findByShipmentId(shipmentId, pageable).map(ShipmentMapper::toTrackingLogResponse);
    }

    @Transactional
    public ShipmentResponse executeAction(Integer shipmentId, ShipmentActionRequest request) {
        Shipment shipment = getShipmentForUpdate(shipmentId);
        ShipmentAction action = request.getAction();

        if (!shipment.getStatus().getAvailableActions().contains(action)) {
            throw new InvalidShipmentOperationException(
                    action.name(),
                    "Action [" + action.name() + "] is not allowed when shipment is " + shipment.getStatus().name()
            );
        }

        switch (action) {
            case HAND_OVER_TO_CARRIER -> {
                shipment.handOverToCarrier(request.getLocation(), request.getReason());
                addTrackingLog(shipment, request.getLocation(), "Shipment handed over to carrier");
                eventPublisher.publishEvent(ShipmentInTransitEvent.now(
                        shipment.getId(), shipment.getOrderId(), shipment.getUserId(), shipment.getTrackingCode()
                ));
            }
            case MARK_DELIVERED -> {
                shipment.markDelivered(request.getLocation(), request.getReason());
                addTrackingLog(shipment, request.getLocation(), "Shipment delivered successfully");
                eventPublisher.publishEvent(ShipmentDeliveredEvent.now(
                        shipment.getId(), shipment.getOrderId(), shipment.getUserId(), shipment.getTrackingCode(), shipment.getCodAmount()
                ));
            }
            case REPORT_DELIVERY_FAILED -> {
                shipment.reportDeliveryFailed(request.getReason());
                addTrackingLog(shipment, request.getLocation(), request.getReason());
            }
            case RETRY_DELIVERY -> {
                shipment.retryDelivery(request.getLocation(), request.getReason());
                addTrackingLog(shipment, request.getLocation(), "Retrying shipment delivery");
            }
            case RETURN_TO_WAREHOUSE -> {
                shipment.returnToWarehouse(request.getReason());
                addTrackingLog(shipment, request.getLocation(), request.getReason());
                eventPublisher.publishEvent(ShipmentReturnedEvent.now(
                        shipment.getId(), shipment.getOrderId(), shipment.getUserId(), shipment.getTrackingCode(), shipment.getFailureReason()
                ));
            }
            case CANCEL_SHIPMENT -> {
                shipment.cancel(request.getReason());
                addTrackingLog(shipment, request.getLocation(), request.getReason());
                eventPublisher.publishEvent(ShipmentCancelledEvent.now(
                        shipment.getId(), shipment.getOrderId(), shipment.getUserId(), shipment.getTrackingCode(), shipment.getFailureReason()
                ));
            }
        }

        shipmentRepository.save(shipment);
        return ShipmentMapper.toResponse(shipment);
    }

    @Transactional
    public void delete(Integer shipmentId) {
        Shipment shipment = getShipment(shipmentId);
        int updatedRows = shipmentRepository.softDeleteWithVersion(
                shipmentId,
                shipment.getVersion(),
                List.of(ShipmentStatus.CANCELLED, ShipmentStatus.RETURNED)
        );
        if (updatedRows == 0) {
            throw new ShipmentDeletedConflictException(shipmentId);
        }
    }

    private Shipment getShipment(Integer shipmentId) {
        return shipmentRepository.findById(shipmentId).orElseThrow(() -> new ShipmentNotFoundException(shipmentId));
    }

    private Shipment getShipmentForUpdate(Integer shipmentId) {
        return shipmentRepository.findByIdForUpdate(shipmentId).orElseThrow(() -> new ShipmentNotFoundException(shipmentId));
    }

    private void addTrackingLog(Shipment shipment, String location, String description) {
        trackingLogRepository.save(new ShipmentTrackingLog(shipment.getId(), shipment.getStatus(), location, description));
    }

    private void validateOwnership(Shipment shipment, Integer currentUserId, boolean isAdminOrStaff, String action) {
        if (!shipment.getUserId().equals(currentUserId) && !isAdminOrStaff) {
            throw new ShipmentAccessDeniedException(shipment.getId(), currentUserId, action);
        }
    }

    private String generateTrackingCode(Integer orderId) {
        String trackingCode;
        do {
            trackingCode = "SHIP-" + LocalDateTime.now().format(TRACKING_DATE_FORMAT) + "-" + orderId;
        } while (shipmentRepository.existsByTrackingCode(trackingCode));
        return trackingCode;
    }
}
