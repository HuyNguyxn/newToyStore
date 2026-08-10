package com.example.new_toy_store.user.api;

import com.example.new_toy_store.user.application.UserFacade;
import com.example.new_toy_store.user.application.dto.request.AddressRequest;
import com.example.new_toy_store.user.application.dto.request.ChangePasswordRequest;
import com.example.new_toy_store.user.application.dto.request.ForgotPasswordRequest;
import com.example.new_toy_store.user.application.dto.request.LoginRequest;
import com.example.new_toy_store.user.application.dto.request.ProfileUpdateRequest;
import com.example.new_toy_store.user.application.dto.request.RegisterRequest;
import com.example.new_toy_store.user.application.dto.request.ResetPasswordRequest;
import com.example.new_toy_store.user.application.dto.request.UpdateUserRoleRequest;
import com.example.new_toy_store.user.application.dto.request.UpdateUserStatusRequest;
import com.example.new_toy_store.user.application.dto.request.UserFilterRequest;
import com.example.new_toy_store.user.application.dto.response.AuthResponse;
import com.example.new_toy_store.user.application.dto.response.DeletedUserAdminResponse;
import com.example.new_toy_store.user.application.dto.response.PasswordResetTokenResponse;
import com.example.new_toy_store.user.application.dto.response.UserAdminResponse;
import com.example.new_toy_store.user.application.dto.response.UserAdminSummaryResponse;
import com.example.new_toy_store.user.application.dto.response.UserProfileResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserFacade facade;

    public UserController(UserFacade facade) {
        this.facade = facade;
    }

    @PostMapping("/register")
    public UserProfileResponse register(@Valid @RequestBody RegisterRequest request) {
        return facade.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return facade.login(request);
    }

    @GetMapping("/verify")
    public void verifyEmail(@RequestParam String token) {
        facade.verifyEmailToken(token);
    }

    @PostMapping("/resend-verification")
    public void resendVerification(@Valid @RequestBody ForgotPasswordRequest request) {
        facade.resendVerificationEmail(request.getEmail());
    }

    @PostMapping("/forgot-password")
    public PasswordResetTokenResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return facade.requestPasswordReset(request);
    }

    @PostMapping("/reset-password")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        facade.resetPassword(request);
    }

    @GetMapping("/me")
    public UserProfileResponse getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return facade.getCurrentProfile(userDetails.getUsername());
    }

    @PutMapping("/me")
    public UserProfileResponse updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return facade.updateCurrentProfile(userDetails.getUsername(), request);
    }

    @PatchMapping("/me/password")
    public void changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        facade.changeCurrentPassword(userDetails.getUsername(), request);
    }

    @GetMapping("/me/profile")
    public UserProfileResponse getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        return getMe(userDetails);
    }

    @PutMapping("/me/profile")
    public UserProfileResponse updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        return updateMe(userDetails, request);
    }

    @PutMapping("/me/password")
    public void changePasswordLegacy(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        changePassword(userDetails, request);
    }

    @PostMapping("/me/addresses")
    public UserProfileResponse addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddressRequest request
    ) {
        return facade.addCurrentAddress(userDetails.getUsername(), request);
    }

    @PatchMapping("/me/addresses/{addressId}/default")
    public UserProfileResponse setDefaultAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @Positive(message = "ID địa chỉ phải lớn hơn 0") Integer addressId
    ) {
        return facade.setCurrentDefaultAddress(userDetails.getUsername(), addressId);
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public UserProfileResponse removeAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @Positive(message = "ID địa chỉ phải lớn hơn 0") Integer addressId
    ) {
        return facade.removeCurrentAddress(userDetails.getUsername(), addressId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserAdminResponse> getUsers(UserFilterRequest request, Pageable pageable) {
        return facade.getUsers(request, pageable);
    }

    @GetMapping("/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public UserAdminSummaryResponse getUserSummary() {
        return facade.getAdminSummary();
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public List<DeletedUserAdminResponse> getDeletedUsers() {
        return facade.getDeletedUsers();
    }

    @PatchMapping("/{id:\\d+}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public void restoreDeletedUser(
            @PathVariable @Positive(message = "ID nguoi dung phai lon hon 0") Integer id
    ) {
        facade.restoreDeletedAccount(id);
    }

    @DeleteMapping("/{id:\\d+}/permanent")
    @PreAuthorize("hasRole('ADMIN')")
    public void permanentlyDeleteDeletedUser(
            @PathVariable @Positive(message = "ID nguoi dung phai lon hon 0") Integer id
    ) {
        facade.permanentlyDeleteDeletedAccount(id);
    }

    @GetMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserAdminResponse getUser(@PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer id) {
        return facade.getUserForAdmin(id);
    }

    @PatchMapping("/{id:\\d+}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserAdminResponse updateRole(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return facade.updateUserRole(id, request);
    }

    @PatchMapping("/{id:\\d+}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public UserAdminResponse updateStatus(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return facade.updateUserStatus(id, request);
    }

    @PatchMapping("/{id:\\d+}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public void lockAccount(@PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer id) {
        facade.lockAccount(id);
    }

    @PatchMapping("/{id:\\d+}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public void unlockAccount(@PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer id) {
        facade.unlockAccount(id);
    }

    @DeleteMapping("/{id:\\d+}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer id) {
        facade.deleteAccount(id);
    }
}
