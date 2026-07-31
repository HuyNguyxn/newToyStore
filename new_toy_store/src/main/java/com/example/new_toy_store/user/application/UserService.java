package com.example.new_toy_store.user.application;

import com.example.new_toy_store.global.event.UserDeletedEvent;
import com.example.new_toy_store.infrastructure.security.jwt.JwtProvider;
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
import com.example.new_toy_store.user.application.dto.response.PasswordResetTokenResponse;
import com.example.new_toy_store.user.application.dto.response.UserAdminResponse;
import com.example.new_toy_store.user.application.dto.response.UserProfileResponse;
import com.example.new_toy_store.user.application.dto.response.NotificationRecipientResponse;
import com.example.new_toy_store.user.application.config.UserProfileProperties;
import com.example.new_toy_store.user.domain.Address;
import com.example.new_toy_store.user.domain.TokenType;
import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.UserRepository;
import com.example.new_toy_store.user.domain.UserRole;
import com.example.new_toy_store.user.domain.UserStatus;
import com.example.new_toy_store.user.domain.VerificationToken;
import com.example.new_toy_store.user.domain.VerificationTokenRepository;
import com.example.new_toy_store.user.domain.exception.InvalidUserOperationException;
import com.example.new_toy_store.user.domain.exception.UserNotFoundException;
import com.example.new_toy_store.user.mapper.UserMapper;
import com.example.new_toy_store.infrastructure.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository repository;
    private final VerificationTokenRepository tokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;
    private final UserProfileProperties profileProperties;

    public UserService(
            UserRepository repository,
            VerificationTokenRepository tokenRepository,
            JwtProvider jwtProvider,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            ApplicationEventPublisher eventPublisher,
            UserProfileProperties profileProperties
    ) {
        this.repository = repository;
        this.tokenRepository = tokenRepository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.eventPublisher = eventPublisher;
        this.profileProperties = profileProperties;
    }

    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        Optional<User> existingUser = repository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            if (existingUser.get().getStatus() == UserStatus.LOCKED) {
                throw InvalidUserOperationException.lockedEmailCannotRegister(request.getEmail());
            }
            throw InvalidUserOperationException.duplicateEmail(request.getEmail());
        }

        List<String> oldStatuses = repository.findStatusesOfSoftDeletedUsersByEmailPattern(request.getEmail());
        if (oldStatuses.contains("LOCKED")) {
            throw InvalidUserOperationException.lockedEmailCannotRegister(request.getEmail());
        }

        List<Integer> softDeletedUserIds = repository.findSoftDeletedUserIdsByEmailPattern(request.getEmail());
        if (!softDeletedUserIds.isEmpty()) {
            repository.hardDeleteAddressesByUserIds(softDeletedUserIds);
            repository.hardDeleteUsersByIds(softDeletedUserIds);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = UserMapper.toEntity(request, encodedPassword);
        repository.save(user);

        String tokenValue = java.util.UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(tokenValue, TokenType.VERIFICATION, user);
        tokenRepository.save(verificationToken);

        return UserMapper.toProfileResponse(user, getDefaultAvatarUrl());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        if (!user.getStatus().canLogin()) {
            throw InvalidUserOperationException.accountCannotLogin(request.getEmail());
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw InvalidUserOperationException.invalidCredentials();
        }

        String token = jwtProvider.generateToken(user);
        return new AuthResponse(token, jwtProvider.getAccessTokenExpirationSeconds(), UserMapper.toProfileResponse(user, getDefaultAvatarUrl()));
    }

    @Transactional
    public void verifyEmailToken(String tokenValue) {
        VerificationToken token = tokenRepository.findByTokenValue(tokenValue)
                .orElseThrow(InvalidUserOperationException::invalidToken);
        if (token.getTokenType() != TokenType.VERIFICATION) {
            throw InvalidUserOperationException.invalidToken();
        }

        if (token.isExpired()) {
            throw InvalidUserOperationException.expiredToken();
        }

        User user = token.getUser();
        user.activate();
        repository.save(user);
        tokenRepository.delete(token);
    }

    @Transactional
    public PasswordResetTokenResponse requestPasswordReset(ForgotPasswordRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(request.getEmail()));

        if (!user.getStatus().canLogin()) {
            throw InvalidUserOperationException.accountCannotLogin(request.getEmail());
        }

        tokenRepository.deleteByUser_IdAndTokenType(user.getId(), TokenType.RESET_PASSWORD);
        VerificationToken token = new VerificationToken(UUID.randomUUID().toString(), TokenType.RESET_PASSWORD, user);
        tokenRepository.save(token);

        return new PasswordResetTokenResponse(
                user.getEmail(),
                token.getTokenValue(),
                token.getExpiryDate(),
                "Use this reset token in POST /users/reset-password. In a real production app, this token should be sent by email."
        );
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        VerificationToken token = tokenRepository.findByTokenValue(request.getToken())
                .orElseThrow(InvalidUserOperationException::invalidToken);
        if (token.getTokenType() != TokenType.RESET_PASSWORD) {
            throw InvalidUserOperationException.invalidToken();
        }
        if (token.isExpired()) {
            throw InvalidUserOperationException.expiredToken();
        }

        User user = token.getUser();
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw InvalidUserOperationException.duplicatedNewPassword();
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
        tokenRepository.delete(token);
    }

    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        User user = getUserEntity(userId);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw InvalidUserOperationException.wrongOldPassword();
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw InvalidUserOperationException.duplicatedNewPassword();
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedNewPassword);
        repository.save(user);
    }

    @Transactional
    public void activateAccount(Integer userId) {
        User user = getUserEntity(userId);
        user.activate();
        repository.save(user);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Integer userId) {
        return UserMapper.toProfileResponse(getUserEntityWithAddresses(userId), getDefaultAvatarUrl());
    }

    @Transactional
    public UserProfileResponse updateProfile(Integer userId, ProfileUpdateRequest request) {
        User user = getUserEntity(userId);
        user.updateProfile(request.getFullName(), request.getPhoneNumber(), request.getAvatarUrl());
        return UserMapper.toProfileResponse(user, getDefaultAvatarUrl());
    }

    @Transactional
    public UserProfileResponse addAddress(Integer userId, AddressRequest request) {
        User user = getUserEntityWithAddresses(userId);
        Address address = new Address(
                request.getReceiverName(),
                request.getReceiverPhone(),
                request.getDetailAddress(),
                request.isDefault()
        );
        user.addAddress(address);
        repository.save(user);
        return UserMapper.toProfileResponse(user, getDefaultAvatarUrl());
    }

    @Transactional
    public UserProfileResponse setAddressDefault(Integer userId, Integer addressId) {
        User user = getUserEntityWithAddresses(userId);
        user.setDefaultAddress(addressId);
        repository.save(user);
        return UserMapper.toProfileResponse(user, getDefaultAvatarUrl());
    }

    @Transactional
    public UserProfileResponse removeAddress(Integer userId, Integer addressId) {
        User user = getUserEntityWithAddresses(userId);
        user.removeAddress(addressId);
        repository.save(user);
        return UserMapper.toProfileResponse(user, getDefaultAvatarUrl());
    }

    @Transactional(readOnly = true)
    public Page<UserAdminResponse> getUsers(UserFilterRequest request, Pageable pageable) {
        return repository.findAll(UserSpecification.filter(request), pageable)
                .map(user -> UserMapper.toAdminResponse(user, getDefaultAvatarUrl()));
    }

    @Transactional(readOnly = true)
    public UserAdminResponse getUserForAdmin(Integer userId) {
        return UserMapper.toAdminResponse(getUserEntity(userId), getDefaultAvatarUrl());
    }

    @Transactional
    public UserAdminResponse updateUserRole(Integer userId, UpdateUserRoleRequest request) {
        User user = getUserEntity(userId);
        user.changeRole(request.getRole());
        repository.save(user);
        return UserMapper.toAdminResponse(user, getDefaultAvatarUrl());
    }

    @Transactional
    public UserAdminResponse updateUserStatus(Integer userId, UpdateUserStatusRequest request) {
        User user = getUserEntity(userId);
        user.changeStatus(request.getStatus());
        repository.save(user);
        return UserMapper.toAdminResponse(user, getDefaultAvatarUrl());
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

    @Transactional
    public void deleteAccount(Integer userId) {
        User user = getUserEntity(userId);
        String deletedEmail = user.getEmail();
        user.delete();
        repository.save(user);
        eventPublisher.publishEvent(UserDeletedEvent.now(userId, deletedEmail));
    }

    public User getAuthenticatedUser(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> InvalidUserOperationException.authenticatedUserMissing(email));
    }

    public User getUserEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public List<User> getUsersByIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return repository.findAllById(ids);
    }

    @Transactional(readOnly = true)
    public NotificationRecipientResponse getNotificationRecipient(Integer userId) {
        User user = getUserEntity(userId);
        return new NotificationRecipientResponse(user.getId(), user.getEmail(), user.getFullName());
    }

    @Transactional(readOnly = true)
    public List<NotificationRecipientResponse> getActiveNotificationRecipients() {
        return repository.findAllByStatus(UserStatus.ACTIVE).stream()
                .map(user -> new NotificationRecipientResponse(user.getId(), user.getEmail(), user.getFullName()))
                .toList();
    }

    private User getUserEntityWithAddresses(Integer id) {
        return repository.findByIdWithAddresses(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private String getDefaultAvatarUrl() {
        return profileProperties.getDefaultAvatarUrl();
    }
}
