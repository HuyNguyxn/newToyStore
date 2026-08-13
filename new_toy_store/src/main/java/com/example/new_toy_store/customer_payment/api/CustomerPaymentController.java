package com.example.new_toy_store.customer_payment.api;

import com.example.new_toy_store.customer_payment.application.service.PaymentService;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentCancelRequest;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentCheckoutRequest;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentConfirmRequest;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentFailureRequest;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentFilterRequest;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentRefundDecisionRequest;
import com.example.new_toy_store.customer_payment.application.dto.request.CustomerPaymentRefundRequest;
import com.example.new_toy_store.customer_payment.application.dto.response.CustomerPaymentRefundResponse;
import com.example.new_toy_store.customer_payment.application.dto.response.CustomerPaymentResponse;
import com.example.new_toy_store.customer_payment.application.dto.response.VnpayReturnResponse;
import com.example.new_toy_store.infrastructure.payment.vnpay.VnpayIpnResponse;
import com.example.new_toy_store.user.application.UserFacade;
import com.example.new_toy_store.user.application.dto.response.UserProfileResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@Validated
public class CustomerPaymentController {

    private final PaymentService service;
    private final UserFacade userFacade;

    public CustomerPaymentController(PaymentService service, UserFacade userFacade) {
        this.service = service;
        this.userFacade = userFacade;
    }

    @PostMapping("/checkout")
    public CustomerPaymentResponse checkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CustomerPaymentCheckoutRequest request,
            HttpServletRequest servletRequest
    ) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        return service.checkout(request, user.getId(), isAdmin(user), getClientIp(servletRequest));
    }

    @GetMapping("/vnpay-return")
    public VnpayReturnResponse handleVnpayReturn(@RequestParam Map<String, String> params) {
        return service.handleVnpayReturn(params);
    }

    @GetMapping("/vnpay-ipn")
    public VnpayIpnResponse handleVnpayIpn(@RequestParam Map<String, String> params) {
        return service.handleVnpayIpn(params);
    }

    @GetMapping("/my-payments")
    public Page<CustomerPaymentResponse> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable
    ) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        return service.getMyPayments(user.getId(), pageable);
    }

    @GetMapping("/orders/{orderId}/latest")
    public CustomerPaymentResponse getLatestPaymentForOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer orderId
    ) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        return service.getLatestPaymentForOrder(orderId, user.getId(), isAdmin(user));
    }

    @GetMapping("/{id}")
    public CustomerPaymentResponse getDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id
    ) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        return service.getDetails(id, user.getId(), isAdmin(user));
    }

    @GetMapping("/admin/filter")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public Page<CustomerPaymentResponse> filter(CustomerPaymentFilterRequest request, Pageable pageable) {
        return service.filter(request, pageable);
    }

    @PostMapping("/{id}/refunds")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomerPaymentRefundResponse requestRefund(
            @PathVariable Integer id,
            @Valid @RequestBody CustomerPaymentRefundRequest request
    ) {
        return service.requestRefund(id, request);
    }

    @GetMapping("/{id}/refunds")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public Page<CustomerPaymentRefundResponse> getRefunds(@PathVariable Integer id, Pageable pageable) {
        return service.getRefunds(id, pageable);
    }

    @GetMapping("/refunds")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public Page<CustomerPaymentRefundResponse> getAllRefunds(Pageable pageable) {
        return service.getAllRefunds(pageable);
    }

    @PatchMapping("/refunds/{refundId}/process")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomerPaymentRefundResponse processRefund(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer refundId,
            HttpServletRequest servletRequest
    ) {
        return service.processRefund(refundId, userDetails.getUsername(), getClientIp(servletRequest));
    }

    @PatchMapping("/refunds/{refundId}/reject")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public CustomerPaymentRefundResponse rejectRefund(
            @PathVariable Integer refundId,
            @Valid @RequestBody CustomerPaymentRefundDecisionRequest request
    ) {
        return service.rejectRefund(refundId, request);
    }

    @DeleteMapping("/refunds/{refundId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteRefund(@PathVariable Integer refundId) {
        service.deleteRefund(refundId);
    }

    @PatchMapping("/{id}/succeed")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public CustomerPaymentResponse markSucceeded(
            @PathVariable Integer id,
            @Valid @RequestBody(required = false) CustomerPaymentConfirmRequest request
    ) {
        return service.markSucceeded(id, request);
    }

    @PatchMapping("/{id}/fail")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public CustomerPaymentResponse markFailed(
            @PathVariable Integer id,
            @Valid @RequestBody CustomerPaymentFailureRequest request
    ) {
        return service.markFailed(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public CustomerPaymentResponse cancel(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody CustomerPaymentCancelRequest request
    ) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        return service.cancel(id, request, user.getId(), isAdmin(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    private UserProfileResponse getAuthenticatedUser(UserDetails userDetails) {
        return userFacade.getCurrentProfile(userDetails.getUsername());
    }

    private boolean isAdmin(UserProfileResponse user) {
        return "ADMIN".equals(user.getRole());
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
