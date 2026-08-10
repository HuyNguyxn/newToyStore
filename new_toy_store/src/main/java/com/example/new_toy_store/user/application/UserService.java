package com.example.new_toy_store.user.application;

import com.example.new_toy_store.global.event.UserDeletedEvent;
import com.example.new_toy_store.infrastructure.mail.MailService;
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
import com.example.new_toy_store.user.application.dto.response.DeletedUserAdminResponse;
import com.example.new_toy_store.user.application.dto.response.PasswordResetTokenResponse;
import com.example.new_toy_store.user.application.dto.response.UserAdminResponse;
import com.example.new_toy_store.user.application.dto.response.UserAdminSummaryResponse;
import com.example.new_toy_store.user.application.dto.response.UserProfileResponse;
import com.example.new_toy_store.user.application.dto.response.NotificationRecipientResponse;
import com.example.new_toy_store.user.application.config.UserProfileProperties;
import com.example.new_toy_store.user.domain.Address;
import com.example.new_toy_store.user.domain.DeletedUserProjection;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
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
    private final MailService mailService;
    private final String frontendBaseUrl;

    public UserService(
            UserRepository repository,
            VerificationTokenRepository tokenRepository,
            JwtProvider jwtProvider,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            ApplicationEventPublisher eventPublisher,
            UserProfileProperties profileProperties,
            MailService mailService,
            @Value("${app.frontend.base-url}") String frontendBaseUrl
    ) {
        this.repository = repository;
        this.tokenRepository = tokenRepository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.eventPublisher = eventPublisher;
        this.profileProperties = profileProperties;
        this.mailService = mailService;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        Optional<User> existingUser = repository.findByEmail(email);
        if (existingUser.isPresent()) {
            if (existingUser.get().getStatus() == UserStatus.LOCKED) {
                throw InvalidUserOperationException.lockedEmailCannotRegister(email);
            }
            throw InvalidUserOperationException.duplicateEmail(email);
        }

        if (repository.findSoftDeletedUserIdByOriginalEmail(email).isPresent()) {
            throw InvalidUserOperationException.deletedEmailCannotRegister(email);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = UserMapper.toEntity(request, encodedPassword, email);
        repository.save(user);

        String tokenValue = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken(tokenValue, TokenType.VERIFICATION, user);
        tokenRepository.save(verificationToken);
        sendVerificationEmail(user, verificationToken);

        return UserMapper.toProfileResponse(user, getDefaultAvatarUrl());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = repository.findByEmail(email)
                .orElseThrow(InvalidUserOperationException::invalidCredentials);

        if (!user.getStatus().canLogin()) {
            throw InvalidUserOperationException.accountCannotLogin(email);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (BadCredentialsException ex) {
            throw InvalidUserOperationException.invalidCredentials();
        } catch (AuthenticationException ex) {
            throw InvalidUserOperationException.invalidCredentials();
        }

        String token = jwtProvider.generateToken(user);
        return new AuthResponse(token, jwtProvider.getAccessTokenExpirationSeconds(), UserMapper.toProfileResponse(user, getDefaultAvatarUrl()));
    }

    @Transactional
    public void verifyEmailToken(String tokenValue) {
        VerificationToken token = tokenRepository.findByTokenValue(normalizeToken(tokenValue))
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
    public void resendVerificationEmail(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        if (user.getStatus() != UserStatus.UNVERIFIED) {
            throw InvalidUserOperationException.verificationNotAllowed(email);
        }

        tokenRepository.deleteByUser_IdAndTokenType(user.getId(), TokenType.VERIFICATION);
        VerificationToken token = new VerificationToken(UUID.randomUUID().toString(), TokenType.VERIFICATION, user);
        tokenRepository.save(token);
        sendVerificationEmail(user, token);
    }

    @Transactional
    public PasswordResetTokenResponse requestPasswordReset(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.getEmail());
        Optional<User> optionalUser = repository.findByEmail(email);
        String genericMessage = "Nếu email tồn tại và tài khoản đang hoạt động, hướng dẫn đặt lại mật khẩu đã được gửi.";
        if (optionalUser.isEmpty() || !optionalUser.get().getStatus().canLogin()) {
            return new PasswordResetTokenResponse(genericMessage);
        }
        User user = optionalUser.get();

        tokenRepository.deleteByUser_IdAndTokenType(user.getId(), TokenType.RESET_PASSWORD);
        VerificationToken token = new VerificationToken(UUID.randomUUID().toString(), TokenType.RESET_PASSWORD, user);
        tokenRepository.save(token);
        sendPasswordResetEmail(user, token);
        return new PasswordResetTokenResponse(genericMessage);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        VerificationToken token = tokenRepository.findByTokenValue(normalizeToken(request.getToken()))
                .orElseThrow(InvalidUserOperationException::invalidToken);
        if (token.getTokenType() != TokenType.RESET_PASSWORD) {
            throw InvalidUserOperationException.invalidToken();
        }
        if (token.isExpired()) {
            throw InvalidUserOperationException.expiredToken();
        }

        User user = token.getUser();
        if (!user.getStatus().canLogin()) {
            throw InvalidUserOperationException.accountCannotLogin(user.getEmail());
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw InvalidUserOperationException.duplicatedNewPassword();
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
        tokenRepository.deleteByUser_IdAndTokenType(user.getId(), TokenType.RESET_PASSWORD);
    }

    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        User user = getUserEntity(userId);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw InvalidUserOperationException.wrongOldPassword();
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw InvalidUserOperationException.duplicatedNewPassword();
        }

        String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
        user.updatePassword(encodedNewPassword);
        repository.save(user);
        tokenRepository.deleteByUser_IdAndTokenType(user.getId(), TokenType.RESET_PASSWORD);
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
    public UserAdminSummaryResponse getAdminSummary() {
        return new UserAdminSummaryResponse(
                repository.count(),
                repository.countByRole(UserRole.ADMIN),
                repository.countByRole(UserRole.MANAGER),
                repository.countByRole(UserRole.STAFF),
                repository.countByRole(UserRole.CUSTOMER),
                repository.countByStatus(UserStatus.ACTIVE),
                repository.countByStatus(UserStatus.LOCKED),
                repository.countByStatus(UserStatus.UNVERIFIED)
        );
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
        tokenRepository.deleteByUser_Id(userId);
        user.delete();
        repository.save(user);
        eventPublisher.publishEvent(UserDeletedEvent.now(userId, deletedEmail));
    }

    @Transactional(readOnly = true)
    public List<DeletedUserAdminResponse> getDeletedUsers() {
        return repository.findAllSoftDeletedUsers().stream()
                .map(this::toDeletedUserResponse)
                .toList();
    }

    @Transactional
    public void restoreDeletedAccount(Integer userId) {
        DeletedUserProjection deletedUser = repository.findAllSoftDeletedUsers().stream()
                .filter(user -> userId.equals(user.getId()))
                .findFirst()
                .orElseThrow(() -> new UserNotFoundException(userId));
        String email = normalizeEmail(deletedUser.getEmail());
        if (repository.existsByEmail(email)) {
            throw InvalidUserOperationException.restoreConflict(email);
        }

        repository.restoreSoftDeletedAddresses(userId, deletedUser.getDeletedAt());
        int restored = repository.restoreSoftDeletedUser(userId, email);
        if (restored == 0) {
            throw new UserNotFoundException(userId);
        }
    }

    @Transactional
    public void permanentlyDeleteDeletedAccount(Integer userId) {
        boolean exists = repository.findAllSoftDeletedUsers().stream()
                .anyMatch(user -> userId.equals(user.getId()));
        if (!exists) {
            throw InvalidUserOperationException.permanentDeleteRequiresDeletedAccount(userId);
        }

        try {
            tokenRepository.deleteAllByUserIdNative(userId);
            repository.deleteAddressesByUserId(userId);
            int deleted = repository.deleteSoftDeletedUserPermanently(userId);
            if (deleted == 0) {
                throw new UserNotFoundException(userId);
            }
        } catch (DataIntegrityViolationException ex) {
            throw InvalidUserOperationException.permanentDeleteBlockedByRelatedData(userId);
        }
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

    private void sendVerificationEmail(User user, VerificationToken token) {
        String verificationLink = frontendBaseUrl.replaceAll("/+$", "")
                + "/verify-email?token=" + token.getTokenValue();
        String body = """
                Xin chào %s,

                Cảm ơn bạn đã đăng ký tài khoản NewToyStore.

                Mã xác thực của bạn là:
                %s

                Bấm vào liên kết dưới đây để xác thực tài khoản:
                %s

                Mã xác thực hết hạn lúc: %s
                """.formatted(
                user.getFullName(),
                token.getTokenValue(),
                verificationLink,
                token.getExpiryDate()
        );
        try {
            mailService.sendEmail(user.getEmail(), "[NewToyStore] Xác thực tài khoản", body);
        } catch (MailException ex) {
            throw InvalidUserOperationException.emailDeliveryFailed(user.getEmail());
        }
    }

    private void sendPasswordResetEmail(User user, VerificationToken token) {
        String resetLink = frontendBaseUrl.replaceAll("/+$", "")
                + "/reset-password?token=" + token.getTokenValue();
        String body = """
                Xin chào %s,

                Chúng tôi nhận được yêu cầu đặt lại mật khẩu NewToyStore của bạn.
                Bấm vào liên kết dưới đây để tạo mật khẩu mới:
                %s

                Liên kết hết hạn lúc: %s
                Nếu bạn không gửi yêu cầu này, hãy bỏ qua email.
                """.formatted(user.getFullName(), resetLink, token.getExpiryDate());
        try {
            mailService.sendEmail(user.getEmail(), "[NewToyStore] Đặt lại mật khẩu", body);
        } catch (MailException ex) {
            throw InvalidUserOperationException.emailDeliveryFailed(user.getEmail());
        }
    }

    private DeletedUserAdminResponse toDeletedUserResponse(DeletedUserProjection user) {
        return new DeletedUserAdminResponse(
                user.getId(), user.getEmail(), user.getFullName(), user.getPhoneNumber(),
                user.getRole(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt(), user.getDeletedAt()
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeToken(String token) {
        return token == null ? "" : token.trim();
    }
}
