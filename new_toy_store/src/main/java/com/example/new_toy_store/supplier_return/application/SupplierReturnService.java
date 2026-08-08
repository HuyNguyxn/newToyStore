package com.example.new_toy_store.supplier_return.application;

import com.example.new_toy_store.global.event.SupplierReturnCompletedEvent;
import com.example.new_toy_store.global.event.SupplierReturnStatusChangedEvent;
import com.example.new_toy_store.imports.application.facade.ImportFacade;
import com.example.new_toy_store.imports.application.dto.response.ImportNoteItemResponse;
import com.example.new_toy_store.imports.application.dto.response.ImportNoteResponse;
import com.example.new_toy_store.imports.domain.ImportStatus;
import com.example.new_toy_store.infrastructure.specification.SupplierReturnSpecification;
import com.example.new_toy_store.supplier.application.facade.SupplierFacade;
import com.example.new_toy_store.supplier_return.application.dto.request.SupplierReturnInspectionRequest;
import com.example.new_toy_store.supplier_return.application.dto.request.SupplierReturnRequest;
import com.example.new_toy_store.supplier_return.application.dto.response.SupplierReturnResponse;
import com.example.new_toy_store.supplier_return.domain.SupplierReturn;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnItem;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnRepository;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnStatus;
import com.example.new_toy_store.supplier_return.domain.exception.DuplicateSupplierReturnException;
import com.example.new_toy_store.supplier_return.domain.exception.InvalidSupplierReturnOperationException;
import com.example.new_toy_store.supplier_return.domain.exception.SupplierReturnNotFoundException;
import com.example.new_toy_store.supplier_return.mapper.SupplierReturnMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupplierReturnService {

    private static final Logger log = LoggerFactory.getLogger(SupplierReturnService.class);

    private final SupplierReturnRepository repository;
    private final SupplierFacade supplierFacade;
    private final ImportFacade importFacade;
    private final ApplicationEventPublisher eventPublisher;

    public SupplierReturnService(SupplierReturnRepository repository,
                                 SupplierFacade supplierFacade,
                                 ImportFacade importFacade,
                                 ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.supplierFacade = supplierFacade;
        this.importFacade = importFacade;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public void processSlaAlerts(int warningHours, int criticalHours) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime warningCutoffTime = now.minusHours(warningHours);
        LocalDateTime criticalCutoffTime = now.minusHours(criticalHours);

        List<SupplierReturn> criticalAlertReturns = repository.findAllByStatusAndUpdatedAtBefore(
                SupplierReturnStatus.PENDING_APPROVAL, criticalCutoffTime);

        for (SupplierReturn returnNote : criticalAlertReturns) {
            log.error("[SLA CRITICAL] Phiếu trả hàng ID {} đã kẹt quá {} giờ. Cần can thiệp khẩn cấp.",
                    returnNote.getId(), criticalHours);
        }

        List<SupplierReturn> warningAlertReturns = repository.findAllByStatusAndUpdatedAtBetween(
                SupplierReturnStatus.PENDING_APPROVAL, criticalCutoffTime, warningCutoffTime);

        for (SupplierReturn returnNote : warningAlertReturns) {
            log.warn("[SLA WARNING] Phiếu trả hàng ID {} đã chờ duyệt hơn {} giờ. Đã gửi nhắc nhở.",
                    returnNote.getId(), warningHours);
        }
    }

    @Transactional(readOnly = true)
    public List<SupplierReturn> getReturnsForCriticalAlert(int criticalHours) {
        LocalDateTime criticalCutoffTime = LocalDateTime.now().minusHours(criticalHours);

        return repository.findAllByStatusAndUpdatedAtBefore(
                SupplierReturnStatus.PENDING_APPROVAL, criticalCutoffTime);
    }

    @Transactional(readOnly = true)
    public Page<SupplierReturnResponse> filterReturns(Integer supplierId,
                                                      String status,
                                                      LocalDate startDate,
                                                      LocalDate endDate,
                                                      Pageable pageable) {
        return repository.findAll(SupplierReturnSpecification.filter(supplierId, status, startDate, endDate), pageable)
                .map(SupplierReturnMapper::toSummaryResponse);
    }

    @Transactional(readOnly = true)
    public SupplierReturnResponse getDetail(Integer id) {
        return SupplierReturnMapper.mapEntityToResponse(getEntity(id));
    }

    @Transactional
    public SupplierReturnResponse createDraft(SupplierReturnRequest request, String adminUsername) {
        supplierFacade.getRequiredSupplierDetails(request.getSupplierId(), "supplier_return");

        validateImportedQuantities(request);

        if (request.getImportNoteId() != null) {
            boolean hasActiveReturn = repository.existsByImportNoteIdAndStatusNotIn(
                    request.getImportNoteId(),
                    List.of(SupplierReturnStatus.CANCELLED, SupplierReturnStatus.REJECTED)
            );

            if (hasActiveReturn) {
                throw new DuplicateSupplierReturnException(request.getImportNoteId());
            }
        }

        SupplierReturn entity = SupplierReturnMapper.mapRequestToNewEntity(request, adminUsername);
        return SupplierReturnMapper.mapEntityToResponse(repository.save(entity));
    }

    private void validateImportedQuantities(SupplierReturnRequest request) {
        if (request.getImportNoteId() == null) {
            return;
        }

        ImportNoteResponse importNote = importFacade.getImportNoteDetails(request.getImportNoteId());
        if (importNote.getStatus() != ImportStatus.COMPLETED) {
            throw InvalidSupplierReturnOperationException.invalidTransition(
                    importNote.getStatus().getDisplayName(), "Tạo phiếu trả hàng");
        }
        if (!request.getSupplierId().equals(importNote.getSupplierId())) {
            throw InvalidSupplierReturnOperationException.emptyField("Nhà cung cấp không khớp với phiếu nhập");
        }

        Map<Integer, Integer> importedByVariant = importNote.getItems().stream()
                .collect(Collectors.toMap(ImportNoteItemResponse::getVariantId, ImportNoteItemResponse::getQuantity, Integer::sum));
        Map<Integer, Integer> requestedByVariant = request.getItems().stream()
                .collect(Collectors.toMap(item -> item.getVariantId(), item -> item.getQuantity(), Integer::sum));

        requestedByVariant.forEach((variantId, requestedQuantity) -> {
            int importedQuantity = importedByVariant.getOrDefault(variantId, 0);
            if (requestedQuantity > importedQuantity) {
                throw InvalidSupplierReturnOperationException.quantityExceedsImported(
                        variantId, requestedQuantity, importedQuantity);
            }
        });
    }

    @Transactional
    public SupplierReturnResponse submitForApproval(Integer id, String adminUsername) {
        SupplierReturn returnNote = getEntity(id);
        SupplierReturnStatus previousStatus = returnNote.getStatus();
        returnNote.submitForApproval(adminUsername, "Trình duyệt phiếu trả hàng");
        SupplierReturn saved = repository.save(returnNote);
        publishStatusChanged(saved, previousStatus, adminUsername);
        return SupplierReturnMapper.mapEntityToResponse(saved);
    }

    @Transactional
    public SupplierReturnResponse approve(Integer id, String managerUsername) {
        SupplierReturn returnNote = getEntity(id);
        SupplierReturnStatus previousStatus = returnNote.getStatus();
        returnNote.approve(managerUsername, "Quản lý đã duyệt xuất trả");
        SupplierReturn saved = repository.save(returnNote);
        publishStatusChanged(saved, previousStatus, managerUsername);
        return SupplierReturnMapper.mapEntityToResponse(saved);
    }

    @Transactional
    public SupplierReturnResponse reject(Integer id, String managerUsername, String reason) {
        SupplierReturn returnNote = getEntity(id);
        SupplierReturnStatus previousStatus = returnNote.getStatus();
        returnNote.reject(managerUsername, "Từ chối: " + reason);
        SupplierReturn saved = repository.save(returnNote);
        publishStatusChanged(saved, previousStatus, managerUsername);
        return SupplierReturnMapper.mapEntityToResponse(saved);
    }

    @Transactional
    public SupplierReturnResponse shipAndDeductStock(Integer id, String warehouseUsername) {
        SupplierReturn returnNote = getEntity(id);
        SupplierReturnStatus previousStatus = returnNote.getStatus();
        returnNote.ship(warehouseUsername, "Xuất kho trả nhà cung cấp");

        List<SupplierReturnCompletedEvent.ReturnItemDetail> eventItems = returnNote.getItems().stream()
                .map(item -> new SupplierReturnCompletedEvent.ReturnItemDetail(
                        item.getVariantId(),
                        item.getBatchNumber(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        SupplierReturn saved = repository.save(returnNote);
        publishStatusChanged(saved, previousStatus, warehouseUsername);
        eventPublisher.publishEvent(new SupplierReturnCompletedEvent(saved.getId(), eventItems));

        return SupplierReturnMapper.mapEntityToResponse(saved);
    }

    @Transactional
    public SupplierReturnResponse markShippingFailed(Integer id, String reason) {
        SupplierReturn returnNote = getEntity(id);
        SupplierReturnStatus previousStatus = returnNote.getStatus();
        returnNote.markShippingFailed("SYSTEM_CARRIER", reason);
        SupplierReturn saved = repository.save(returnNote);
        publishStatusChanged(saved, previousStatus, "SYSTEM_CARRIER");
        return SupplierReturnMapper.mapEntityToResponse(saved);
    }

    @Transactional
    public SupplierReturnResponse recordInspection(Integer id, SupplierReturnInspectionRequest request, String username) {
        SupplierReturn returnNote = getEntity(id);

        Map<Integer, Integer> qtyMap = request.getItems().stream()
                .collect(Collectors.toMap(
                        SupplierReturnInspectionRequest.ItemInspection::getItemId,
                        SupplierReturnInspectionRequest.ItemInspection::getAcceptedQuantity
                ));

        Map<Integer, String> reasonMap = request.getItems().stream()
                .collect(Collectors.toMap(
                        SupplierReturnInspectionRequest.ItemInspection::getItemId,
                        item -> item.getDiscrepancyReason() != null ? item.getDiscrepancyReason() : ""
                ));

        returnNote.recordInspection(qtyMap, reasonMap, username);

        return SupplierReturnMapper.mapEntityToResponse(repository.save(returnNote));
    }

    @Transactional
    public SupplierReturnResponse complete(Integer id, String accountantUsername) {
        SupplierReturn returnNote = getEntity(id);
        SupplierReturnStatus previousStatus = returnNote.getStatus();
        returnNote.complete(accountantUsername, "Xác nhận nhận tiền/cấn trừ công nợ hoàn tất");
        SupplierReturn saved = repository.save(returnNote);
        publishStatusChanged(saved, previousStatus, accountantUsername);
        return SupplierReturnMapper.mapEntityToResponse(saved);
    }

    private void publishStatusChanged(SupplierReturn returnNote,
                                      SupplierReturnStatus previousStatus,
                                      String actionBy) {
        if (previousStatus != returnNote.getStatus()) {
            eventPublisher.publishEvent(SupplierReturnStatusChangedEvent.now(
                    returnNote.getId(),
                    returnNote.getSupplierId(),
                    returnNote.getImportNoteId(),
                    previousStatus,
                    returnNote.getStatus(),
                    actionBy
            ));
        }
    }

    private SupplierReturn getEntity(Integer id) {
        return repository.findById(id).orElseThrow(() -> new SupplierReturnNotFoundException(id));
    }
}
