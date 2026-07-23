package com.example.new_toy_store.user.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidUserOperationException extends UserDomainException {

    private InvalidUserOperationException(HttpStatus status, String errorType, String message) {
        this(status, errorType, message, Map.of());
    }

    private InvalidUserOperationException(
            HttpStatus status,
            String errorType,
            String message,
            Map<String, ?> contextData
    ) {
        super(status, errorType, message, contextData);
    }

    public static InvalidUserOperationException duplicateEmail(String email) {
        return new InvalidUserOperationException(
                HttpStatus.CONFLICT,
                "Trùng lặp email đang hoạt động",
                "Email đã được sử dụng bởi một tài khoản đang hoạt động",
                Map.of("email", safe(email))
        );
    }

    public static InvalidUserOperationException lockedEmailCannotRegister(String email) {
        return new InvalidUserOperationException(
                HttpStatus.CONFLICT,
                "Email bị khóa",
                "Email này đang bị khóa hoặc nằm trong danh sách đen của hệ thống",
                Map.of("email", safe(email))
        );
    }

    public static InvalidUserOperationException invalidCredentials() {
        return new InvalidUserOperationException(
                HttpStatus.UNAUTHORIZED,
                "Thông tin đăng nhập không hợp lệ",
                "Email hoặc mật khẩu không chính xác"
        );
    }

    public static InvalidUserOperationException accountCannotLogin(String email) {
        return new InvalidUserOperationException(
                HttpStatus.FORBIDDEN,
                "Tài khoản không được phép đăng nhập",
                "Tài khoản chưa được kích hoạt hoặc đang bị khóa",
                Map.of("email", safe(email))
        );
    }

    public static InvalidUserOperationException invalidToken() {
        return new InvalidUserOperationException(
                HttpStatus.BAD_REQUEST,
                "Token không hợp lệ",
                "Token xác thực không hợp lệ"
        );
    }

    public static InvalidUserOperationException expiredToken() {
        return new InvalidUserOperationException(
                HttpStatus.BAD_REQUEST,
                "Token đã hết hạn",
                "Token xác thực đã hết hạn"
        );
    }

    public static InvalidUserOperationException wrongOldPassword() {
        return new InvalidUserOperationException(
                HttpStatus.BAD_REQUEST,
                "Mật khẩu cũ không chính xác",
                "Mật khẩu cũ không chính xác"
        );
    }

    public static InvalidUserOperationException duplicatedNewPassword() {
        return new InvalidUserOperationException(
                HttpStatus.BAD_REQUEST,
                "Mật khẩu mới không hợp lệ",
                "Mật khẩu mới không được trùng mật khẩu cũ"
        );
    }

    public static InvalidUserOperationException authenticatedUserMissing(String email) {
        return new InvalidUserOperationException(
                HttpStatus.UNAUTHORIZED,
                "Không tìm thấy người dùng đang đăng nhập",
                "Token hợp lệ nhưng không tìm thấy người dùng tương ứng trong hệ thống",
                Map.of("email", safe(email))
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
