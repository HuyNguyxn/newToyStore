package com.example.new_toy_store.user.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class UserNotFoundException extends UserDomainException {

    public UserNotFoundException(Integer userId) {
        super(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy người dùng",
                "Không tìm thấy người dùng với ID: " + userId,
                Map.of("userId", userId == null ? "" : userId)
        );
    }

    public UserNotFoundException(String email) {
        super(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy người dùng",
                "Không tìm thấy người dùng với email: " + email,
                Map.of("email", email == null ? "" : email)
        );
    }
}
