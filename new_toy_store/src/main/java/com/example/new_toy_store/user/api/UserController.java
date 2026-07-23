package com.example.new_toy_store.user.api;

import com.example.new_toy_store.user.application.UserService;
import com.example.new_toy_store.user.application.dto.request.AddressRequest;
import com.example.new_toy_store.user.application.dto.request.ChangePasswordRequest;
import com.example.new_toy_store.user.application.dto.request.LoginRequest;
import com.example.new_toy_store.user.application.dto.request.ProfileUpdateRequest;
import com.example.new_toy_store.user.application.dto.request.RegisterRequest;
import com.example.new_toy_store.user.application.dto.request.UpdateUserRoleRequest;
import com.example.new_toy_store.user.application.dto.request.UpdateUserStatusRequest;
import com.example.new_toy_store.user.application.dto.request.UserFilterRequest;
import com.example.new_toy_store.user.application.dto.response.AuthResponse;
import com.example.new_toy_store.user.application.dto.response.UserAdminResponse;
import com.example.new_toy_store.user.application.dto.response.UserProfileResponse;
import com.example.new_toy_store.user.domain.User;
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

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public UserProfileResponse register(@Valid @RequestBody RegisterRequest request) {
        return service.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    @GetMapping("/verify")
    public void verifyEmail(@RequestParam String token) {
        service.verifyEmailToken(token);
    }

    @GetMapping("/me")
    public UserProfileResponse getMe(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getAuthenticatedUser(userDetails);
        return service.getProfile(user.getId());
    }

    @PutMapping("/me")
    public UserProfileResponse updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        User user = getAuthenticatedUser(userDetails);
        return service.updateProfile(user.getId(), request);
    }

    @PatchMapping("/me/password")
    public void changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        User user = getAuthenticatedUser(userDetails);
        service.changePassword(user.getId(), request);
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
        User user = getAuthenticatedUser(userDetails);
        return service.addAddress(user.getId(), request);
    }

    @PatchMapping("/me/addresses/{addressId}/default")
    public UserProfileResponse setDefaultAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @Positive(message = "ID địa chỉ phải lớn hơn 0") Integer addressId
    ) {
        User user = getAuthenticatedUser(userDetails);
        return service.setAddressDefault(user.getId(), addressId);
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public UserProfileResponse removeAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @Positive(message = "ID địa chỉ phải lớn hơn 0") Integer addressId
    ) {
        User user = getAuthenticatedUser(userDetails);
        return service.removeAddress(user.getId(), addressId);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<UserAdminResponse> getUsers(UserFilterRequest request, Pageable pageable) {
        return service.getUsers(request, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserAdminResponse getUser(@PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer id) {
        return service.getUserForAdmin(id);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserAdminResponse updateRole(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer id,
            @Valid @RequestBody UpdateUserRoleRequest request
    ) {
        return service.updateUserRole(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public UserAdminResponse updateStatus(
            @PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer id,
            @Valid @RequestBody UpdateUserStatusRequest request
    ) {
        return service.updateUserStatus(id, request);
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public void lockAccount(@PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer id) {
        service.lockAccount(id);
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public void unlockAccount(@PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer id) {
        service.unlockAccount(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable @Positive(message = "ID người dùng phải lớn hơn 0") Integer id) {
        service.deleteAccount(id);
    }

    private User getAuthenticatedUser(UserDetails userDetails) {
        return service.getAuthenticatedUser(userDetails.getUsername());
    }
}
