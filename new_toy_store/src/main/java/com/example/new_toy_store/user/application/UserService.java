package com.example.new_toy_store.user.application;

import com.example.new_toy_store.cart.application.CartService;
import com.example.new_toy_store.infrastructure.security.jwt.JwtProvider;
import com.example.new_toy_store.user.application.dto.request.*;
import com.example.new_toy_store.user.application.dto.response.*;
import com.example.new_toy_store.user.domain.*;
import com.example.new_toy_store.user.mapper.UserMapper;
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
    private final CartService cartService;

    public UserService(UserRepository repository,
                       VerificationTokenRepository tokenRepository,
                       JwtProvider jwtProvider,
                       PasswordEncoder passwordEncoder,
                       CartService cartService) {
        this.repository = repository;
        this.tokenRepository = tokenRepository;
        this.jwtProvider = jwtProvider;
        this.passwordEncoder = passwordEncoder;
        this.cartService = cartService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        Optional<User> existingUser = repository.findByEmail(request.getEmail());
        if (existingUser.isPresent()) {
            if (existingUser.get().getStatus() == UserStatus.LOCKED) {
                throw new IllegalStateException("Tài khoản này đang bị khóa, không thể đăng ký lại.");
            }
            throw new IllegalArgumentException("Email đã được sử dụng.");
        }

        List<String> oldStatuses = repository.findStatusesOfSoftDeletedUsersByEmailPattern(request.getEmail());
        if (oldStatuses.contains("LOCKED")) {
            throw new IllegalStateException("Email này nằm trong danh sách đen của hệ thống. Không thể tạo tài khoản mới!");
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

        return UserMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        if (!user.getStatus().canLogin()) {
            throw new IllegalStateException("Tài khoản chưa được kích hoạt hoặc đang bị khóa");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu không chính xác");
        }

        String token = jwtProvider.generateToken(user);
        return new AuthResponse(token, UserMapper.toResponse(user));
    }

    @Transactional
    public void verifyEmailToken(String tokenValue) {
        VerificationToken token = tokenRepository.findByTokenValue(tokenValue)
                .orElseThrow(() -> new RuntimeException("Token không hợp lệ"));

        if (token.isExpired()) {
            throw new RuntimeException("Token đã hết hạn");
        }

        User user = token.getUser();
        user.activate();
        repository.save(user);
    }

    @Transactional
    public void changePassword(Integer userId, ChangePasswordRequest request) {
        User user = getUserEntity(userId);

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
        }

        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng mật khẩu cũ");
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

    @Transactional
    public void deleteAccount(Integer userId) {
        User user = getUserEntity(userId);
        user.delete();
        repository.save(user);
        cartService.clearCart(userId);
    }

    private User getUserEntity(Integer id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm tháy người dùng."));
    }

    private User getUserEntityWithAddresses(Integer id) {
        return repository.findByIdWithAddresses(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng."));
    }
}