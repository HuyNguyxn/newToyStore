package com.example.new_toy_store.user.api;

import com.example.new_toy_store.user.application.UserService;
import com.example.new_toy_store.user.application.dto.request.AddressRequest;
import com.example.new_toy_store.user.application.dto.request.ProfileUpdateRequest;
import com.example.new_toy_store.user.application.dto.request.RegisterRequest;
import com.example.new_toy_store.user.application.dto.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return service.register(request);
    }

    @GetMapping("/{id}/profile")
    public UserResponse getProfile(@PathVariable Integer id) {
        return service.getProfile(id);
    }

    @PutMapping("/{id}/profile")
    public UserResponse updateProfile(@PathVariable Integer id, @Valid @RequestBody ProfileUpdateRequest request) {
        return service.updateProfile(id, request);
    }

    @PostMapping("/{id}/addresses")
    public UserResponse addAddress(@PathVariable Integer id, @Valid @RequestBody AddressRequest request) {
        return service.addAddress(id, request);
    }

    @PatchMapping("/{id}/addresses/{addressId}/default")
    public UserResponse setDefaultAddress(@PathVariable Integer id, @PathVariable Integer addressId) {
        return service.setAddressDefault(id, addressId);
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    public UserResponse removeAddress(@PathVariable Integer id, @PathVariable Integer addressId) {
        return service.removeAddress(id, addressId);
    }

    @PatchMapping("/{id}/lock")
    public void lockAccount(@PathVariable Integer id) {
        service.lockAccount(id);
    }

    @PatchMapping("/{id}/unlock")
    public void unlockAccount(@PathVariable Integer id) {
        service.unlockAccount(id);
    }
}