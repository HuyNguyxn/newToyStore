package com.example.new_toy_store.customer_return.api;

import com.example.new_toy_store.customer_return.application.CustomerReturnService;
import com.example.new_toy_store.customer_return.application.dto.request.CustomerReturnRequest;
import com.example.new_toy_store.customer_return.application.dto.response.CustomerReturnResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
    public CustomerReturnResponse create(@Valid @RequestBody CustomerReturnRequest request) {
        String currentUser = "customer_demo"; // tam thoi
        return service.createRequest(request, currentUser);
    }

    @PatchMapping("/{id}/cancel")
    public CustomerReturnResponse cancelRequest(@PathVariable Integer id) {
        return service.cancelRequest(id, "customer_demo");
    }

    @PatchMapping("/{id}/update-info")
    public CustomerReturnResponse updateInfo(@PathVariable Integer id, @RequestParam String newReasonNote) {
        return service.updateInfoByCustomer(id, "customer_demo", newReasonNote);
    }

    @PatchMapping("/{id}/require-info")
    public CustomerReturnResponse requireInfo(@PathVariable Integer id, @RequestParam String adminMessage) {
        return service.requireMoreInfo(id, "admin_demo", adminMessage);
    }

    @PatchMapping("/{id}/receive")
    public CustomerReturnResponse receive(@PathVariable Integer id) {
        return service.receiveItems(id, "warehouse_demo");
    }

    @PatchMapping("/{id}/inspect")
    public CustomerReturnResponse inspectQuality(
            @PathVariable Integer id,
            @RequestParam boolean isPassed,
            @RequestParam String qcNote) {
        return service.inspectQuality(id, "qc_demo", isPassed, qcNote);
    }

    @PatchMapping("/{id}/dispute")
    public CustomerReturnResponse openDispute(
            @PathVariable Integer id,
            @RequestParam String disputeReason) {
        return service.createDispute(id, "customer_demo", disputeReason);
    }

    @PatchMapping("/{id}/resolve-dispute")
    public CustomerReturnResponse resolveDispute(@PathVariable Integer id, @RequestParam String resolutionNote) {
        return service.resolveDisputeToRefund(id, "admin_demo", resolutionNote);
    }
}