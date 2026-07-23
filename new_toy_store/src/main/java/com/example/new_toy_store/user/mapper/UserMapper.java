package com.example.new_toy_store.user.mapper;

import com.example.new_toy_store.user.application.dto.request.RegisterRequest;
import com.example.new_toy_store.user.application.dto.response.AddressResponse;
import com.example.new_toy_store.user.application.dto.response.UserAdminResponse;
import com.example.new_toy_store.user.application.dto.response.UserProfileResponse;
import com.example.new_toy_store.user.application.dto.response.UserResponse;
import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.UserRole;
import com.example.new_toy_store.user.domain.UserStatus;

import java.util.List;
import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {
    }

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
        UserRole role = user.getRole();
        UserStatus status = user.getStatus();

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                resolveAvatarUrl(user),
                role.name(),
                role,
                status.name(),
                status,
                status.getNextValidStates(),
                determineProfileActions(user),
                toAddressResponses(user)
        );
    }

    public static UserAdminResponse toAdminResponse(User user) {
        UserRole role = user.getRole();
        UserStatus status = user.getStatus();

        return new UserAdminResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                resolveAvatarUrl(user),
                role.name(),
                role.getDisplayName(),
                role,
                status.name(),
                status.getDisplayName(),
                status,
                status.getNextValidStates(),
                UserRole.ADMIN.getAssignableRoles(),
                determineAdminActions(user),
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
                .map(UserMapper::toAddressResponse)
                .collect(Collectors.toList());
    }

    private static AddressResponse toAddressResponse(com.example.new_toy_store.user.domain.Address address) {
        return new AddressResponse(
                address.getId(),
                address.getReceiverName(),
                address.getReceiverPhone(),
                address.getDetailAddress(),
                address.isDefault()
        );
    }

    private static List<String> determineProfileActions(User user) {
        if (!user.getStatus().canModifyData()) {
            return List.of("VIEW_PROFILE");
        }

        return List.of(
                "VIEW_PROFILE",
                "UPDATE_PROFILE",
                "CHANGE_PASSWORD",
                "ADD_ADDRESS",
                "UPDATE_ADDRESS",
                "REMOVE_ADDRESS"
        );
    }

    private static List<String> determineAdminActions(User user) {
        if (user.getStatus() == UserStatus.LOCKED) {
            return List.of("VIEW_USER", "UNLOCK_USER", "CHANGE_ROLE", "DELETE_USER");
        }
        if (user.getStatus() == UserStatus.UNVERIFIED) {
            return List.of("VIEW_USER", "ACTIVATE_USER", "LOCK_USER", "CHANGE_ROLE", "DELETE_USER");
        }
        return List.of("VIEW_USER", "LOCK_USER", "CHANGE_ROLE", "DELETE_USER");
    }

    private static String resolveAvatarUrl(User user) {
        String defaultAvatar = "/assets/default-avatar.png";
        return user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()
                ? user.getAvatarUrl()
                : defaultAvatar;
    }
}
