package com.example.new_toy_store.user.application;

import com.example.new_toy_store.global.event.UserDeletedEvent;
import com.example.new_toy_store.infrastructure.security.jwt.JwtProvider;
import com.example.new_toy_store.user.application.dto.request.AddressRequest;
import com.example.new_toy_store.user.application.dto.request.ChangePasswordRequest;
import com.example.new_toy_store.user.application.dto.request.LoginRequest;
import com.example.new_toy_store.user.application.dto.request.ProfileUpdateRequest;
import com.example.new_toy_store.user.application.dto.request.RegisterRequest;
import com.example.new_toy_store.user.application.dto.request.UpdateUserRoleRequest;
import com.example.new_toy_store.user.application.dto.request.UpdateUserStatusRequest;
import com.example.new_toy_store.user.application.dto.request.UserFilterRequest;
import com.example.new_toy_store.user.application.dto.response.AuthResponse;
import com.example.new_toy_store.user.application.dto.response.UserAdminResponse;
import com.example.new_toy_store.user.application.dto.response.UserProfileResponse;
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

@Service
public class UserService {

    private final UserRepository repository;
    private final VerificationTokenRepository tokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(
            UserRepository repository,
            VerificationTokenRepository tokenRepository,
            JwtProvider jwtProvider,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            ApplicationEventPublisher eventPublisher
    ) {
        this.repository = repository;
        this.tokenRepository = tokenRepository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.eventPublisher = eventPublisher;
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

        return UserMapper.toProfileResponse(user);
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
        return new AuthResponse(token, jwtProvider.getAccessTokenExpirationSeconds(), UserMapper.toProfileResponse(user));
    }

    @Transactional
    public void verifyEmailToken(String tokenValue) {
        VerificationToken token = tokenRepository.findByTokenValue(tokenValue)
                .orElseThrow(InvalidUserOperationException::invalidToken);

        if (token.isExpired()) {
            throw InvalidUserOperationException.expiredToken();
        }

        User user = token.getUser();
        user.activate();
        repository.save(user);
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
        return UserMapper.toProfileResponse(getUserEntityWithAddresses(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(Integer userId, ProfileUpdateRequest request) {
        User user = getUserEntity(userId);
        user.updateProfile(request.getFullName(), request.getPhoneNumber(), request.getAvatarUrl());
        return UserMapper.toProfileResponse(user);
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
        return UserMapper.toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse setAddressDefault(Integer userId, Integer addressId) {
        User user = getUserEntityWithAddresses(userId);
        user.setDefaultAddress(addressId);
        repository.save(user);
        return UserMapper.toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse removeAddress(Integer userId, Integer addressId) {
        User user = getUserEntityWithAddresses(userId);
        user.removeAddress(addressId);
        repository.save(user);
        return UserMapper.toProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserAdminResponse> getUsers(UserFilterRequest request, Pageable pageable) {
        return repository.findAll(UserSpecification.filter(request), pageable)
                .map(UserMapper::toAdminResponse);
    }

    @Transactional(readOnly = true)
    public UserAdminResponse getUserForAdmin(Integer userId) {
        return UserMapper.toAdminResponse(getUserEntity(userId));
    }

    @Transactional
    public UserAdminResponse updateUserRole(Integer userId, UpdateUserRoleRequest request) {
        User user = getUserEntity(userId);
        user.changeRole(UserRole.from(request.getRole()));
        repository.save(user);
        return UserMapper.toAdminResponse(user);
    }

    @Transactional
    public UserAdminResponse updateUserStatus(Integer userId, UpdateUserStatusRequest request) {
        User user = getUserEntity(userId);
        user.changeStatus(UserStatus.from(request.getStatus()));
        repository.save(user);
        return UserMapper.toAdminResponse(user);
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

    private User getUserEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private User getUserEntityWithAddresses(Integer id) {
        return repository.findByIdWithAddresses(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
