package com.example.new_toy_store.supplier_return.application;

import com.example.new_toy_store.global.event.SupplierReturnCompletedEvent;
import com.example.new_toy_store.supplier.application.SupplierService;
import com.example.new_toy_store.supplier_return.application.dto.request.SupplierReturnInspectionRequest;
import com.example.new_toy_store.supplier_return.application.dto.request.SupplierReturnRequest;
import com.example.new_toy_store.supplier_return.application.dto.response.SupplierReturnResponse;
import com.example.new_toy_store.supplier_return.domain.SupplierReturn;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnItem;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnRepository;
import com.example.new_toy_store.supplier_return.domain.SupplierReturnStatus;
import com.example.new_toy_store.supplier_return.domain.exception.DuplicateSupplierReturnException;
import com.example.new_toy_store.supplier_return.domain.exception.SupplierReturnNotFoundException;
import com.example.new_toy_store.infrastructure.specification.SupplierReturnSpecification;
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

    private final SupplierService supplierService;

    private final ApplicationEventPublisher eventPublisher;

    public SupplierReturnService(
            SupplierReturnRepository repository,
            SupplierService supplierService,
            ApplicationEventPublisher eventPublisher) {

        this.repository = repository;
        this.supplierService = supplierService;
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
            log.error("[SLA CRITICAL] Phiếu trả hàng ID {} đã kẹt quá {} giờ! Cần can thiệp khẩn cấp.",
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
    public Page<SupplierReturnResponse> filterReturns(Integer supplierId, String status, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return repository.findAll(SupplierReturnSpecification.filter(supplierId, status, startDate, endDate), pageable)
                .map(SupplierReturnMapper::mapEntityToResponse);
    }

    @Transactional(readOnly = true)
    public SupplierReturnResponse getDetail(Integer id) {
        return SupplierReturnMapper.mapEntityToResponse(getEntity(id));
    }

    @Transactional
    public SupplierReturnResponse createDraft(SupplierReturnRequest request, String adminUsername) {
        supplierService.getSupplierDetails(request.getSupplierId());

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

    @Transactional
    public SupplierReturnResponse submitForApproval(Integer id, String adminUsername) {
        SupplierReturn returnNote = getEntity(id);
        returnNote.submitForApproval(adminUsername, "Trình duyệt phiếu trả hàng");

        return SupplierReturnMapper.mapEntityToResponse(repository.save(returnNote));
    }

    @Transactional
    public SupplierReturnResponse approve(Integer id, String managerUsername) {
        SupplierReturn returnNote = getEntity(id);
        returnNote.approve(managerUsername, "Quản lý đã duyệt xuất trả");

        return SupplierReturnMapper.mapEntityToResponse(repository.save(returnNote));
    }

    @Transactional
    public SupplierReturnResponse reject(Integer id, String managerUsername, String reason) {
        SupplierReturn returnNote = getEntity(id);
        returnNote.reject(managerUsername, "Từ chối: " + reason);

        return SupplierReturnMapper.mapEntityToResponse(repository.save(returnNote));
    }

    @Transactional
    public SupplierReturnResponse shipAndDeductStock(Integer id, String warehouseUsername) {
        SupplierReturn returnNote = getEntity(id);
        returnNote.ship(warehouseUsername, "Xuất kho trả nhà cung cấp");

        List<SupplierReturnCompletedEvent.ReturnItemDetail> eventItems = returnNote.getItems().stream()
                .map(item -> new SupplierReturnCompletedEvent.ReturnItemDetail(
                        item.getVariantId(),
                        item.getBatchNumber(),
                        item.getQuantity()
                ))
                .collect(Collectors.toList());

        eventPublisher.publishEvent(new SupplierReturnCompletedEvent(returnNote.getId(), eventItems));

        return SupplierReturnMapper.mapEntityToResponse(repository.save(returnNote));
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
        returnNote.complete(accountantUsername, "Xác nhận nhận tiền/cấn trừ công nợ hoàn tất");

        return SupplierReturnMapper.mapEntityToResponse(repository.save(returnNote));
    }

    private SupplierReturn getEntity(Integer id) {
        return repository.findById(id).orElseThrow(() -> new SupplierReturnNotFoundException(id));
    }
}