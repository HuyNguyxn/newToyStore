package com.example.new_toy_store.user.mapper;

import com.example.new_toy_store.user.application.dto.request.RegisterRequest;
import com.example.new_toy_store.user.application.dto.response.AddressResponse;
import com.example.new_toy_store.user.application.dto.response.UserResponse;
import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.UserRole;

import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    public static User toEntity(RegisterRequest request, String encodedPassword) {
        return new User(
                request.getEmail(),
                encodedPassword,
                request.getFullName(),
                request.getPhoneNumber(),
                UserRole.CUSTOMER
        );
    }

    public static UserResponse toResponse(User user) {
        String defaultAvatar = "/assets/default-avatar.png";
        String finalAvatar = (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty())
                ? user.getAvatarUrl() : defaultAvatar;

        List<AddressResponse> addressResponses = user.getAddresses().stream()
                .map(a -> new AddressResponse(
                        a.getId(),
                        a.getReceiverName(),
                        a.getReceiverPhone(),
                        a.getDetailAddress(),
                        a.isDefault()
                ))
                .collect(Collectors.toList());

        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                finalAvatar,
                user.getRole().name(),
                user.getStatus().name(),
                addressResponses
        );
    }
}