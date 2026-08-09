package com.example.new_toy_store.user.mapper;

import com.example.new_toy_store.user.application.dto.request.RegisterRequest;
import com.example.new_toy_store.user.application.dto.response.AddressResponse;
import com.example.new_toy_store.user.application.dto.response.UserAdminResponse;
import com.example.new_toy_store.user.application.dto.response.UserEnumOptionResponse;
import com.example.new_toy_store.user.application.dto.response.UserProfileResponse;
import com.example.new_toy_store.user.application.dto.response.UserResponse;
import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.UserRole;
import com.example.new_toy_store.user.domain.UserStatus;

import java.util.List;
import java.util.stream.Collectors;

public final class UserMapper {

    private static final String FALLBACK_DEFAULT_AVATAR_URL = "";

    private UserMapper() {
    }

    public static User toEntity(RegisterRequest request, String encodedPassword, String normalizedEmail) {
        return new User(
                normalizedEmail,
                encodedPassword,
                request.getFullName(),
                request.getPhoneNumber(),
                UserRole.CUSTOMER
        );
    }

    public static UserProfileResponse toProfileResponse(User user) {
        return toProfileResponse(user, FALLBACK_DEFAULT_AVATAR_URL);
    }

    public static UserProfileResponse toProfileResponse(User user, String defaultAvatarUrl) {
        UserRole role = user.getRole();
        UserStatus status = user.getStatus();

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                resolveAvatarUrl(user, defaultAvatarUrl),
                role.name(),
                toRoleOption(role),
                status.name(),
                toStatusOption(status),
                toStatusOptions(status.getNextValidStates()),
                determineProfileActions(user),
                toAddressResponses(user)
        );
    }

    public static UserAdminResponse toAdminResponse(User user) {
        return toAdminResponse(user, FALLBACK_DEFAULT_AVATAR_URL);
    }

    public static UserAdminResponse toAdminResponse(User user, String defaultAvatarUrl) {
        UserRole role = user.getRole();
        UserStatus status = user.getStatus();

        return new UserAdminResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                resolveAvatarUrl(user, defaultAvatarUrl),
                role.name(),
                role.getDisplayName(),
                toRoleOption(role),
                status.name(),
                status.getDisplayName(),
                toStatusOption(status),
                toStatusOptions(status.getNextValidStates()),
                toRoleOptions(UserRole.ADMIN.getAssignableRoles()),
                determineAdminActions(user),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public static UserResponse toResponse(User user) {
        return toResponse(user, FALLBACK_DEFAULT_AVATAR_URL);
    }

    public static UserResponse toResponse(User user, String defaultAvatarUrl) {
        UserProfileResponse profile = toProfileResponse(user, defaultAvatarUrl);
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

    private static UserEnumOptionResponse toRoleOption(UserRole role) {
        if (role == null) {
            return null;
        }

        return new UserEnumOptionResponse(
                role.getCode(),
                role.name(),
                role.getDisplayName(),
                null,
                null,
                null,
                role.canManageProducts(),
                role.canManageOrders()
        );
    }

    private static List<UserEnumOptionResponse> toRoleOptions(List<UserRole> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }

        return roles.stream()
                .map(UserMapper::toRoleOption)
                .collect(Collectors.toList());
    }

    private static UserEnumOptionResponse toStatusOption(UserStatus status) {
        if (status == null) {
            return null;
        }

        return new UserEnumOptionResponse(
                status.getCode(),
                status.name(),
                status.getDisplayName(),
                status.canLogin(),
                status.canPlaceOrder(),
                status.canModifyData(),
                null,
                null
        );
    }

    private static List<UserEnumOptionResponse> toStatusOptions(List<UserStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }

        return statuses.stream()
                .map(UserMapper::toStatusOption)
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

    private static String resolveAvatarUrl(User user, String defaultAvatarUrl) {
        String defaultAvatar = defaultAvatarUrl != null && !defaultAvatarUrl.trim().isEmpty()
                ? defaultAvatarUrl.trim()
                : FALLBACK_DEFAULT_AVATAR_URL;
        return user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()
                ? user.getAvatarUrl()
                : defaultAvatar;
    }
}
