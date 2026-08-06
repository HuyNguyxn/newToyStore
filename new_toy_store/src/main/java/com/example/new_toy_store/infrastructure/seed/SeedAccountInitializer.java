package com.example.new_toy_store.infrastructure.seed;

import com.example.new_toy_store.user.domain.User;
import com.example.new_toy_store.user.domain.UserRepository;
import com.example.new_toy_store.user.domain.UserRole;
import com.example.new_toy_store.user.domain.UserStatus;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class SeedAccountInitializer implements ApplicationRunner {

    private final SeedAccountProperties properties;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedAccountInitializer(
            SeedAccountProperties properties,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createAccountIfMissing(properties.getAdmin(), UserRole.ADMIN);
        createAccountIfMissing(properties.getStaff(), UserRole.STAFF);
        createAccountIfMissing(properties.getManager(), UserRole.MANAGER);
        createAccountIfMissing(properties.getCustomer(), UserRole.CUSTOMER);
    }

    private void createAccountIfMissing(SeedAccountProperties.Account account, UserRole role) {
        if (account == null || !account.isEnabled()) {
            return;
        }
        if (isBlank(account.getEmail()) || isBlank(account.getPassword()) || isBlank(account.getFullName())) {
            return;
        }
        if (userRepository.existsByEmail(account.getEmail())) {
            return;
        }

        User user = new User(
                account.getEmail().trim(),
                passwordEncoder.encode(account.getPassword()),
                account.getFullName().trim(),
                account.getPhoneNumber(),
                role
        );
        user.changeStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
