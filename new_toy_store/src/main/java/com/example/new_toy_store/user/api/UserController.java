package com.example.new_toy_store.user.api;

import com.example.new_toy_store.user.application.UserService;
import com.example.new_toy_store.user.application.dto.request.AddressRequest;
import com.example.new_toy_store.user.application.dto.request.ChangePasswordRequest;
import com.example.new_toy_store.user.application.dto.request.LoginRequest;
import com.example.new_toy_store.user.application.dto.request.ProfileUpdateRequest;
import com.example.new_toy_store.user.application.dto.request.RegisterRequest;
import com.example.new_toy_store.user.application.dto.response.AuthResponse;
import com.example.new_toy_store.user.application.dto.response.UserResponse;
import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;
    private final UserRepository userRepository;

    public UserController(UserService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
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

    @GetMapping("/me/profile")
    public UserResponse getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getAuthenticatedUser(userDetails);
        return service.getProfile(user.getId());
    }

    @PutMapping("/me/profile")
    public UserResponse updateProfile(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ProfileUpdateRequest request) {
        User user = getAuthenticatedUser(userDetails);
        return service.updateProfile(user.getId(), request);
    }

    @PutMapping("/me/password")
    public void changePassword(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ChangePasswordRequest request) {
        User user = getAuthenticatedUser(userDetails);
        service.changePassword(user.getId(), request);
    }

    @PostMapping("/me/addresses")
    public UserResponse addAddress(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody AddressRequest request) {
        User user = getAuthenticatedUser(userDetails);
        return service.addAddress(user.getId(), request);
    }

    @PatchMapping("/me/addresses/{addressId}/default")
    public UserResponse setDefaultAddress(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer addressId) {
        User user = getAuthenticatedUser(userDetails);
        return service.setAddressDefault(user.getId(), addressId);
    }

    @DeleteMapping("/me/addresses/{addressId}")
    public UserResponse removeAddress(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Integer addressId) {
        User user = getAuthenticatedUser(userDetails);
        return service.removeAddress(user.getId(), addressId);
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN')")
    public void lockAccount(@PathVariable Integer id) {
        service.lockAccount(id);
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public void unlockAccount(@PathVariable Integer id) {
        service.unlockAccount(id);
    }

    private User getAuthenticatedUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
    }
}