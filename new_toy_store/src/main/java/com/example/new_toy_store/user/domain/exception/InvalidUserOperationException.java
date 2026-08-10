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

    public static InvalidUserOperationException deletedEmailCannotRegister(String email) {
        return new InvalidUserOperationException(
                HttpStatus.CONFLICT,
                "Tài khoản đã bị xóa",
                "Email này thuộc một tài khoản đã xóa. Vui lòng liên hệ quản trị viên để khôi phục tài khoản",
                Map.of("email", safe(email))
        );
    }

    public static InvalidUserOperationException verificationNotAllowed(String email) {
        return new InvalidUserOperationException(
                HttpStatus.CONFLICT,
                "Không thể gửi lại email xác thực",
                "Chỉ tài khoản chưa xác thực mới có thể nhận lại email xác thực",
                Map.of("email", safe(email))
        );
    }

    public static InvalidUserOperationException restoreConflict(String email) {
        return new InvalidUserOperationException(
                HttpStatus.CONFLICT,
                "Không thể khôi phục tài khoản",
                "Email của tài khoản đã được sử dụng bởi một tài khoản khác",
                Map.of("email", safe(email))
        );
    }

    public static InvalidUserOperationException permanentDeleteRequiresDeletedAccount(Integer userId) {
        return new InvalidUserOperationException(
                HttpStatus.CONFLICT,
                "Không thể xóa vĩnh viễn tài khoản",
                "Chỉ tài khoản đã xóa mềm mới có thể bị xóa vĩnh viễn",
                Map.of("userId", userId == null ? "" : userId)
        );
    }

    public static InvalidUserOperationException permanentDeleteBlockedByRelatedData(Integer userId) {
        return new InvalidUserOperationException(
                HttpStatus.CONFLICT,
                "Không thể xóa vĩnh viễn tài khoản",
                "Tài khoản đang có dữ liệu liên quan nên không thể xóa vĩnh viễn",
                Map.of("userId", userId == null ? "" : userId)
        );
    }

    public static InvalidUserOperationException emailDeliveryFailed(String email) {
        return new InvalidUserOperationException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Không thể gửi email",
                "Không thể gửi email xác thực. Vui lòng kiểm tra cấu hình Gmail trong file .env rồi thử lại",
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

    public static InvalidUserOperationException invalidStatusTransition(String currentStatus, String targetStatus) {
        return new InvalidUserOperationException(
                HttpStatus.CONFLICT,
                "Xung đột trạng thái người dùng",
                "Không thể chuyển trạng thái người dùng từ " + currentStatus + " sang " + targetStatus,
                Map.of("currentStatus", safe(currentStatus), "targetStatus", safe(targetStatus))
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

    public static InvalidUserOperationException inputDataInvalid(String field, String reason) {
        return new InvalidUserOperationException(
                HttpStatus.BAD_REQUEST,
                "Dữ liệu đầu vào không hợp lệ",
                reason,
                Map.of("field", safe(field), "reason", safe(reason))
        );
    }

    public static InvalidUserOperationException invalidRole(String value) {
        return new InvalidUserOperationException(
                HttpStatus.BAD_REQUEST,
                "Vai trò người dùng không hợp lệ",
                "Vai trò người dùng không hợp lệ: " + value,
                Map.of("enum", "UserRole", "value", safe(value))
        );
    }

    public static InvalidUserOperationException invalidStatus(String value) {
        return new InvalidUserOperationException(
                HttpStatus.BAD_REQUEST,
                "Trạng thái người dùng không hợp lệ",
                "Trạng thái người dùng không hợp lệ: " + value,
                Map.of("enum", "UserStatus", "value", safe(value))
        );
    }

    public static InvalidUserOperationException invalidTokenType(String value) {
        return new InvalidUserOperationException(
                HttpStatus.BAD_REQUEST,
                "Loại token không hợp lệ",
                "Loại token không hợp lệ: " + value,
                Map.of("enum", "TokenType", "value", safe(value))
        );
    }

    public static InvalidUserOperationException accountModificationBlocked(String status) {
        return new InvalidUserOperationException(
                HttpStatus.FORBIDDEN,
                "Không có quyền sửa dữ liệu tài khoản",
                "Tài khoản đang ở trạng thái " + status + ", không thể thực hiện thay đổi dữ liệu",
                Map.of("status", safe(status))
        );
    }

    public static InvalidUserOperationException activationConflict(String status) {
        return new InvalidUserOperationException(
                HttpStatus.CONFLICT,
                "Xung đột kích hoạt tài khoản",
                "Chỉ tài khoản chưa xác thực mới có thể kích hoạt",
                Map.of("currentStatus", safe(status), "requiredStatus", "UNVERIFIED")
        );
    }

    public static InvalidUserOperationException addressNotFound(Integer addressId) {
        return new InvalidUserOperationException(
                HttpStatus.NOT_FOUND,
                "Không tìm thấy địa chỉ",
                "Không tìm thấy địa chỉ với ID: " + addressId,
                Map.of("addressId", addressId == null ? "" : addressId)
        );
    }

    public static InvalidUserOperationException accessDenied(String action, Integer userId) {
        return new InvalidUserOperationException(
                HttpStatus.FORBIDDEN,
                "Không có quyền thao tác người dùng",
                "Bạn không có quyền thực hiện thao tác " + action + " trên người dùng này",
                Map.of("action", safe(action), "userId", userId == null ? "" : userId)
        );
    }

    public static InvalidUserOperationException softDeletedConflict(String entityName, Integer entityId) {
        return new InvalidUserOperationException(
                HttpStatus.CONFLICT,
                "Xung đột dữ liệu đã xóa mềm",
                entityName + " đã bị xóa mềm nên không thể tiếp tục thao tác",
                Map.of("entity", safe(entityName), "entityId", entityId == null ? "" : entityId)
        );
    }

    public static InvalidUserOperationException duplicateActiveData(String field, String value) {
        return new InvalidUserOperationException(
                HttpStatus.CONFLICT,
                "Trùng lặp dữ liệu đang hoạt động",
                "Dữ liệu " + field + " đang được sử dụng bởi một bản ghi đang hoạt động",
                Map.of("field", safe(field), "value", safe(value))
        );
    }

    public static InvalidUserOperationException crossModuleDataMismatch(String module, String reason) {
        return new InvalidUserOperationException(
                HttpStatus.BAD_REQUEST,
                "Gửi sai dữ liệu giữa module",
                "Dữ liệu gửi sang module " + module + " không hợp lệ: " + reason,
                Map.of("module", safe(module), "reason", safe(reason))
        );
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
