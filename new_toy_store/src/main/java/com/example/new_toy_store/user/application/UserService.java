package com.example.new_toy_store.user.application;

import com.example.new_toy_store.user.application.dto.request.AddressRequest;
import com.example.new_toy_store.user.application.dto.request.ProfileUpdateRequest;
import com.example.new_toy_store.user.application.dto.request.RegisterRequest;
import com.example.new_toy_store.user.application.dto.response.UserResponse;
import com.example.new_toy_store.user.domain.Address;
import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.UserRepository;
import com.example.new_toy_store.user.mapper.UserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (repository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        String fakeEncodedPassword = "{bcrypt}" + request.getPassword();

        User user = UserMapper.toEntity(request, fakeEncodedPassword);
        repository.save(user);

        return UserMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Integer userId) {
        return UserMapper.toResponse(getUserEntityWithAddresses(userId));
    }

    @Transactional
    public UserResponse updateProfile(Integer userId, ProfileUpdateRequest request) {
        User user = getUserEntity(userId);
        user.updateProfile(request.getFullName(), request.getPhoneNumber(), request.getAvatarUrl());
        return UserMapper.toResponse(user);
    }

    @Transactional
    public UserResponse addAddress(Integer userId, AddressRequest request) {
        User user = getUserEntityWithAddresses(userId);
        Address address = new Address(
                request.getReceiverName(),
                request.getReceiverPhone(),
                request.getDetailAddress(),
                request.isDefault()
        );
        user.addAddress(address);
        repository.save(user);
        return UserMapper.toResponse(user);
    }

    @Transactional
    public UserResponse setAddressDefault(Integer userId, Integer addressId) {
        User user = getUserEntityWithAddresses(userId);
        user.setDefaultAddress(addressId);
        repository.save(user);
        return UserMapper.toResponse(user);
    }

    @Transactional
    public UserResponse removeAddress(Integer userId, Integer addressId) {
        User user = getUserEntityWithAddresses(userId);
        user.removeAddress(addressId);
        repository.save(user);
        return UserMapper.toResponse(user);
    }

    @Transactional
    public void lockAccount(Integer userId) {
        User user = getUserEntity(userId);
        user.lockAccount();
    }

    @Transactional
    public void unlockAccount(Integer userId) {
        User user = getUserEntity(userId);
        user.unlockAccount();
    }

    private User getUserEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private User getUserEntityWithAddresses(Integer id) {
        return repository.findByIdWithAddresses(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}