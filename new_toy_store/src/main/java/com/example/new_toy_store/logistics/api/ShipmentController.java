package com.example.new_toy_store.logistics.api;

import com.example.new_toy_store.logistics.application.LogisticsService;
import com.example.new_toy_store.logistics.application.dto.request.ShipmentActionRequest;
import com.example.new_toy_store.logistics.application.dto.request.ShipmentFilterRequest;
import com.example.new_toy_store.logistics.application.dto.response.ShipmentResponse;
import com.example.new_toy_store.logistics.application.dto.response.ShipmentTrackingLogResponse;
import com.example.new_toy_store.user.application.UserFacade;
import com.example.new_toy_store.user.application.dto.response.UserProfileResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shipments")
@Validated
public class ShipmentController {

    private final LogisticsService service;
    private final UserFacade userFacade;

    public ShipmentController(LogisticsService service, UserFacade userFacade) {
        this.service = service;
        this.userFacade = userFacade;
    }

    @PostMapping("/orders/{orderId}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ShipmentResponse createForConfirmedOrder(@PathVariable Integer orderId) {
        return service.createForConfirmedOrder(orderId);
    }

    @GetMapping("/my-shipments")
    public Page<ShipmentResponse> getMyShipments(@AuthenticationPrincipal UserDetails userDetails, Pageable pageable) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        return service.getMyShipments(user.getId(), pageable);
    }

    @GetMapping("/{id}")
    public ShipmentResponse getDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id
    ) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        return service.getDetails(id, user.getId(), isStaff(user));
    }

    @GetMapping("/{id}/tracking-logs")
    public Page<ShipmentTrackingLogResponse> getTrackingLogs(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id,
            Pageable pageable
    ) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        return service.getTrackingLogs(id, user.getId(), isStaff(user), pageable);
    }

    @GetMapping("/admin/filter")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public Page<ShipmentResponse> filter(ShipmentFilterRequest request, Pageable pageable) {
        return service.filter(request, pageable);
    }

    @PostMapping("/{id}/actions")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public ShipmentResponse executeAction(
            @PathVariable Integer id,
            @Valid @RequestBody ShipmentActionRequest request
    ) {
        return service.executeAction(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    private UserProfileResponse getAuthenticatedUser(UserDetails userDetails) {
        return userFacade.getCurrentProfile(userDetails.getUsername());
    }

    private boolean isStaff(UserProfileResponse user) {
        String role = user.getRole();
        return "STAFF".equals(role) || "MANAGER".equals(role) || "ADMIN".equals(role);
    }
}
