package com.example.new_toy_store.supplier.domain.exception;

import com.example.new_toy_store.supplier.domain.SupplierStatus;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class InvalidSupplierOperationException extends SupplierDomainException {

    private InvalidSupplierOperationException(String message, String errorCode, Map<String, Object> contextData) {
        super(HttpStatus.BAD_REQUEST, errorCode, message, contextData);
    }

    public String getErrorType() { return getErrorCode(); }

    public static InvalidSupplierOperationException emptyField(String fieldName) {
        return new InvalidSupplierOperationException(
                "Trường dữ liệu không hợp lệ: " + fieldName + " không được để trống.",
                "SUPPLIER_EMPTY_FIELD",
                Map.of("fieldName", fieldName)
        );
    }

    public static InvalidSupplierOperationException invalidStatus(String invalidValue) {
        return new InvalidSupplierOperationException(
                "Trạng thái nhà cung cấp không hợp lệ: '" + invalidValue + "'.",
                "SUPPLIER_INVALID_STATUS",
                Map.of(
                        "invalidValue", invalidValue,
                        "allowedValues", SupplierStatus.values()
                )
        );
    }

    public static InvalidSupplierOperationException stillActive(Integer id) {
        return new InvalidSupplierOperationException(
                "Nhà cung cấp ID " + id + " vẫn đang hoạt động, thao tác khôi phục bị từ chối.",
                "SUPPLIER_RESTORE_ACTIVE_RECORD",
                Map.of(
                        "supplierId", id,
                        "currentState", "ACTIVE_RECORD",
                        "attemptedAction", "RESTORE"
                )
        );
    }

    public static InvalidSupplierOperationException invalidTransition(
            Integer supplierId,
            SupplierStatus currentStatus,
            SupplierStatus targetStatus
    ) {
        Map<String, Object> context = new HashMap<>();
        context.put("supplierId", supplierId);
        context.put("currentStatus", currentStatus);
        context.put("targetStatus", targetStatus);
        context.put("allowedNextStatuses", currentStatus.getNextValidStates());

        return new InvalidSupplierOperationException(
                "Không thể chuyển trạng thái nhà cung cấp ID " + supplierId
                        + " từ " + currentStatus.getDisplayName()
                        + " sang " + targetStatus.getDisplayName() + ".",
                "SUPPLIER_INVALID_STATUS_TRANSITION",
                context
        );
    }

    public static InvalidSupplierOperationException missingExternalReference(String fieldName, String sourceModule) {
        return new InvalidSupplierOperationException(
                "Thiếu dữ liệu liên kết nhà cung cấp từ module " + sourceModule + ": " + fieldName + ".",
                "SUPPLIER_MISSING_EXTERNAL_REFERENCE",
                Map.of(
                        "fieldName", fieldName,
                        "sourceModule", sourceModule,
                        "suggestedAction", "SEND_VALID_SUPPLIER_ID"
                )
        );
    }
}
