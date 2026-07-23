package com.example.new_toy_store.user.mapper;

import com.example.new_toy_store.user.application.dto.request.RegisterRequest;
import com.example.new_toy_store.user.application.dto.response.AddressResponse;
import com.example.new_toy_store.user.application.dto.response.UserAdminResponse;
import com.example.new_toy_store.user.application.dto.response.UserProfileResponse;
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

    public static UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                resolveAvatarUrl(user),
                user.getRole().name(),
                user.getStatus().name(),
                toAddressResponses(user)
        );
    }

    public static UserAdminResponse toAdminResponse(User user) {
        return new UserAdminResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                resolveAvatarUrl(user),
                user.getRole().name(),
                user.getRole().getDisplayName(),
                user.getStatus().name(),
                user.getStatus().getDisplayName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static UserResponse toResponse(User user) {
        UserProfileResponse profile = toProfileResponse(user);
        return new UserResponse(
                profile.getId(),
                profile.getEmail(),
                profile.getFullName(),
                profile.getPhoneNumber(),
                profile.getAvatarUrl(),
                profile.getRole(),
                profile.getStatus(),
                profile.getAddresses()
        );
    }

    private static List<AddressResponse> toAddressResponses(User user) {
        return user.getAddresses().stream()
                .map(a -> new AddressResponse(
                        a.getId(),
                        a.getReceiverName(),
                        a.getReceiverPhone(),
                        a.getDetailAddress(),
                        a.isDefault()
                ))
                .collect(Collectors.toList());
    }

    private static String resolveAvatarUrl(User user) {
        String defaultAvatar = "/assets/default-avatar.png";
        return user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()
                ? user.getAvatarUrl()
                : defaultAvatar;
    }
}
