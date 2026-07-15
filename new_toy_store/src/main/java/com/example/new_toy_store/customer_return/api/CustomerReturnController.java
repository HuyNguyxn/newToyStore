package com.example.new_toy_store.customer_return.api;

import com.example.new_toy_store.customer_return.application.CustomerReturnService;
import com.example.new_toy_store.customer_return.application.dto.request.CustomerReturnRequest;
import com.example.new_toy_store.customer_return.application.dto.response.CustomerReturnResponse;
import com.example.new_toy_store.infrastructure.security.service.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/returns")
public class CustomerReturnController {

    private final CustomerReturnService service;

    public CustomerReturnController(CustomerReturnService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CustomerReturnResponse> filter(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer orderId,
            Pageable pageable) {
        return service.filterReturns(status, orderId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerReturnResponse create(
            @Valid @RequestBody CustomerReturnRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return service.createRequest(request, currentUser.getId(), currentUser.getUsername());
    }

    @PatchMapping("/{id}/cancel")
    public CustomerReturnResponse cancelRequest(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return service.cancelRequest(id, currentUser.getId(), currentUser.getUsername());
    }

    @PatchMapping("/{id}/update-info")
    public CustomerReturnResponse updateInfo(
            @PathVariable Integer id,
            @RequestParam String newReasonNote,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return service.updateInfoByCustomer(id, currentUser.getId(), currentUser.getUsername(), newReasonNote);
    }

    @PatchMapping("/{id}/dispute")
    public CustomerReturnResponse openDispute(
            @PathVariable Integer id,
            @RequestParam String disputeReason,
            @AuthenticationPrincipal CustomUserDetails currentUser) {
        return service.createDispute(id, currentUser.getId(), currentUser.getUsername(), disputeReason);
    }

    @PatchMapping("/{id}/require-info")
    public CustomerReturnResponse requireInfo(
            @PathVariable Integer id,
            @RequestParam String adminMessage,
            @AuthenticationPrincipal CustomUserDetails adminUser) {
        return service.requireMoreInfo(id, adminUser.getUsername(), adminMessage);
    }

    @PatchMapping("/{id}/receive")
    public CustomerReturnResponse receive(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails warehouseUser) {
        return service.receiveItems(id, warehouseUser.getUsername());
    }

    @PatchMapping("/{id}/inspect")
    public CustomerReturnResponse inspectQuality(
            @PathVariable Integer id,
            @RequestParam boolean isPassed,
            @RequestParam String qcNote,
            @AuthenticationPrincipal CustomUserDetails qcUser) {
        return service.inspectQuality(id, qcUser.getUsername(), isPassed, qcNote);
    }

    @PatchMapping("/{id}/resolve-dispute")
    public CustomerReturnResponse resolveDispute(
            @PathVariable Integer id,
            @RequestParam boolean isApproved,
            @RequestParam String resolutionNote,
            @AuthenticationPrincipal CustomUserDetails adminUser) {
        return service.resolveDispute(id, adminUser.getUsername(), isApproved, resolutionNote);
    }

    @PatchMapping("/{id}/finalize-refund")
    public CustomerReturnResponse finalizeRefund(
            @PathVariable Integer id,
            @RequestParam String note,
            @AuthenticationPrincipal CustomUserDetails adminUser) {
        return service.finalizeRefundProcess(id, adminUser.getUsername(), note);
    }
}