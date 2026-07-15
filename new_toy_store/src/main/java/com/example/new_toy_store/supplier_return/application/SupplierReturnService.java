package com.example.new_toy_store.supplier_return.application;

import com.example.new_toy_store.product.application.ProductService;
import com.example.new_toy_store.supplier.application.SupplierService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupplierReturnService {

    private final SupplierReturnRepository repository;
    private final ProductService productService;
    private final SupplierService supplierService;

    public SupplierReturnService(
            SupplierReturnRepository repository,
            ProductService productService,
            SupplierService supplierService) {

        this.repository = repository;
        this.productService = productService;
        this.supplierService = supplierService;
    }

    @Transactional(readOnly = true)
    public Page<SupplierReturnResponse> filterReturns(
            Integer supplierId,
            String status,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        return repository.findAll(
                SupplierReturnSpecification.filter(supplierId, status, startDate, endDate),
                pageable
        ).map(SupplierReturnMapper::mapEntityToResponse);
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

        Map<Integer, Integer> deductQuantities = returnNote.getItems().stream()
                .collect(Collectors.toMap(
                        SupplierReturnItem::getVariantId,
                        SupplierReturnItem::getQuantity,
                        Integer::sum
                ));

        productService.deductStockForOrder(deductQuantities);

        return SupplierReturnMapper.mapEntityToResponse(repository.save(returnNote));
    }

    @Transactional
    public SupplierReturnResponse complete(Integer id, String accountantUsername) {
        SupplierReturn returnNote = getEntity(id);
        returnNote.complete(accountantUsername, "Xác nhận nhận tiền/cấn trừ công nợ hoàn tất");

        return SupplierReturnMapper.mapEntityToResponse(repository.save(returnNote));
    }

    private SupplierReturn getEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new SupplierReturnNotFoundException(id));
    }
}