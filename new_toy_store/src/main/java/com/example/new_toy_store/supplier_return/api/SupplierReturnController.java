package com.example.new_toy_store.supplier_return.api;

import com.example.new_toy_store.infrastructure.security.service.CustomUserDetails;
import com.example.new_toy_store.supplier_return.application.SupplierReturnService;
import com.example.new_toy_store.supplier_return.application.dto.request.SupplierReturnRequest;
import com.example.new_toy_store.supplier_return.application.dto.response.SupplierReturnResponse;
import com.example.new_toy_store.supplier_return.domain.exception.SupplierReturnAccessDeniedException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/supplier-returns")
public class SupplierReturnController {

    private final SupplierReturnService service;

    public SupplierReturnController(SupplierReturnService service) {
        this.service = service;
    }

    @GetMapping
    public Page<SupplierReturnResponse> filter(
            @RequestParam(required = false) Integer supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {

        return service.filterReturns(supplierId, status, startDate, endDate, pageable);
    }

    @GetMapping("/{id}")
    public SupplierReturnResponse getDetail(@PathVariable Integer id) {
        return service.getDetail(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SupplierReturnResponse createDraft(
            @Valid @RequestBody SupplierReturnRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        checkWarehouseOrAdminRole(currentUser, "Tạo Phiếu trả hàng");
        return service.createDraft(request, currentUser.getUsername());
    }

    @PatchMapping("/{id}/submit")
    public SupplierReturnResponse submitForApproval(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        checkWarehouseOrAdminRole(currentUser, "Trình duyệt");
        return service.submitForApproval(id, currentUser.getUsername());
    }

    @PatchMapping("/{id}/approve")
    public SupplierReturnResponse approve(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        checkAdminRole(currentUser, "Duyệt xuất trả");
        return service.approve(id, currentUser.getUsername());
    }

    @PatchMapping("/{id}/reject")
    public SupplierReturnResponse reject(
            @PathVariable Integer id,
            @RequestParam String reason,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        checkAdminRole(currentUser, "Từ chối phiếu");
        return service.reject(id, currentUser.getUsername(), reason);
    }

    @PatchMapping("/{id}/ship")
    public SupplierReturnResponse shipAndDeductStock(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        checkWarehouseOrAdminRole(currentUser, "Xuất kho");
        return service.shipAndDeductStock(id, currentUser.getUsername());
    }

    @PatchMapping("/{id}/complete")
    public SupplierReturnResponse complete(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        checkAdminRole(currentUser, "Xác nhận cấn trừ tiền");
        return service.complete(id, currentUser.getUsername());
    }

    private void checkAdminRole(CustomUserDetails user, String action) {
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            throw new SupplierReturnAccessDeniedException(user.getUsername(), action);
        }
    }

    private void checkWarehouseOrAdminRole(CustomUserDetails user, String action) {
        boolean hasAccess = user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_WAREHOUSE_MANAGER"));

        if (!hasAccess) {
            throw new SupplierReturnAccessDeniedException(user.getUsername(), action);
        }
    }
}