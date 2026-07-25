package com.example.new_toy_store.payment.api;

import com.example.new_toy_store.payment.application.PaymentService;
import com.example.new_toy_store.payment.application.dto.request.PaymentCancelRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentCheckoutRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentConfirmRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentFailureRequest;
import com.example.new_toy_store.payment.application.dto.request.PaymentFilterRequest;
import com.example.new_toy_store.payment.application.dto.response.PaymentResponse;
import com.example.new_toy_store.payment.application.dto.response.VnpayReturnResponse;
import com.example.new_toy_store.payment.infrastructure.vnpay.VnpayIpnResponse;
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
public class PaymentController {

    private final PaymentService service;
    private final UserFacade userFacade;

    public PaymentController(PaymentService service, UserFacade userFacade) {
        this.service = service;
        this.userFacade = userFacade;
    }

    @PostMapping("/checkout")
    public PaymentResponse checkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PaymentCheckoutRequest request,
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
    public Page<PaymentResponse> getMyPayments(
            @AuthenticationPrincipal UserDetails userDetails,
            Pageable pageable
    ) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        return service.getMyPayments(user.getId(), pageable);
    }

    @GetMapping("/{id}")
    public PaymentResponse getDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id
    ) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        return service.getDetails(id, user.getId(), isAdmin(user));
    }

    @GetMapping("/admin/filter")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public Page<PaymentResponse> filter(PaymentFilterRequest request, Pageable pageable) {
        return service.filter(request, pageable);
    }

    @PatchMapping("/{id}/succeed")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public PaymentResponse markSucceeded(
            @PathVariable Integer id,
            @Valid @RequestBody(required = false) PaymentConfirmRequest request
    ) {
        return service.markSucceeded(id, request);
    }

    @PatchMapping("/{id}/fail")
    @PreAuthorize("hasAnyRole('STAFF', 'MANAGER', 'ADMIN')")
    public PaymentResponse markFailed(
            @PathVariable Integer id,
            @Valid @RequestBody PaymentFailureRequest request
    ) {
        return service.markFailed(id, request);
    }

    @PatchMapping("/{id}/cancel")
    public PaymentResponse cancel(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id,
            @Valid @RequestBody PaymentCancelRequest request
    ) {
        UserProfileResponse user = getAuthenticatedUser(userDetails);
        return service.cancel(id, request, user.getId(), isAdmin(user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
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
