package com.example.new_toy_store.user.application;

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
import com.example.new_toy_store.user.application.dto.response.NotificationRecipientResponse;
import com.example.new_toy_store.user.application.dto.response.UserAdminResponse;
import com.example.new_toy_store.user.application.dto.response.UserAdminSummaryResponse;
import com.example.new_toy_store.user.application.dto.response.UserProfileResponse;
import com.example.new_toy_store.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class UserFacade {

    private final UserService userService;

    public UserFacade(UserService userService) {
        this.userService = userService;
    }

    public UserProfileResponse register(RegisterRequest request) {
        return userService.register(request);
    }

    public AuthResponse login(LoginRequest request) {
        return userService.login(request);
    }

    public void verifyEmailToken(String token) {
        userService.verifyEmailToken(token);
    }

    public void resendVerificationEmail(String email) {
        userService.resendVerificationEmail(email);
    }

    public PasswordResetTokenResponse requestPasswordReset(ForgotPasswordRequest request) {
        return userService.requestPasswordReset(request);
    }

    public void resetPassword(ResetPasswordRequest request) {
        userService.resetPassword(request);
    }

    public UserProfileResponse getCurrentProfile(String email) {
        User user = userService.getAuthenticatedUser(email);
        return userService.getProfile(user.getId());
    }

    public Integer getAuthenticatedUserId(String email) {
        return userService.getAuthenticatedUser(email).getId();
    }

    public User getRequiredUser(Integer id) {
        return userService.getUserEntity(id);
    }

    public List<User> getUsersByIds(Set<Integer> ids) {
        return userService.getUsersByIds(ids);
    }

    public NotificationRecipientResponse getNotificationRecipient(Integer userId) {
        return userService.getNotificationRecipient(userId);
    }

    public List<NotificationRecipientResponse> getActiveNotificationRecipients() {
        return userService.getActiveNotificationRecipients();
    }

    public UserProfileResponse updateCurrentProfile(String email, ProfileUpdateRequest request) {
        User user = userService.getAuthenticatedUser(email);
        return userService.updateProfile(user.getId(), request);
    }

    public void changeCurrentPassword(String email, ChangePasswordRequest request) {
        User user = userService.getAuthenticatedUser(email);
        userService.changePassword(user.getId(), request);
    }

    public UserProfileResponse addCurrentAddress(String email, AddressRequest request) {
        User user = userService.getAuthenticatedUser(email);
        return userService.addAddress(user.getId(), request);
    }

    public UserProfileResponse setCurrentDefaultAddress(String email, Integer addressId) {
        User user = userService.getAuthenticatedUser(email);
        return userService.setAddressDefault(user.getId(), addressId);
    }

    public UserProfileResponse removeCurrentAddress(String email, Integer addressId) {
        User user = userService.getAuthenticatedUser(email);
        return userService.removeAddress(user.getId(), addressId);
    }

    public Page<UserAdminResponse> getUsers(UserFilterRequest request, Pageable pageable) {
        return userService.getUsers(request, pageable);
    }

    public UserAdminSummaryResponse getAdminSummary() {
        return userService.getAdminSummary();
    }

    public UserAdminResponse getUserForAdmin(Integer id) {
        return userService.getUserForAdmin(id);
    }

    public UserAdminResponse updateUserRole(Integer id, UpdateUserRoleRequest request) {
        return userService.updateUserRole(id, request);
    }

    public UserAdminResponse updateUserStatus(Integer id, UpdateUserStatusRequest request) {
        return userService.updateUserStatus(id, request);
    }

    public void lockAccount(Integer id) {
        userService.lockAccount(id);
    }

    public void unlockAccount(Integer id) {
        userService.unlockAccount(id);
    }

    public void deleteAccount(Integer id) {
        userService.deleteAccount(id);
    }

    public List<DeletedUserAdminResponse> getDeletedUsers() {
        return userService.getDeletedUsers();
    }

    public void restoreDeletedAccount(Integer id) {
        userService.restoreDeletedAccount(id);
    }
}
