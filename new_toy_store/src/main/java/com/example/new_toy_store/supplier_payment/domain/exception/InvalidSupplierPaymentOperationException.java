package com.example.new_toy_store.supplier_payment.domain.exception;

import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentMethod;
import com.example.new_toy_store.supplier_payment.domain.SupplierPaymentStatus;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Map;

public class InvalidSupplierPaymentOperationException extends SupplierPaymentDomainException {

    private InvalidSupplierPaymentOperationException(String message, String errorCode, Map<String, Object> contextData) {
        super(HttpStatus.BAD_REQUEST, errorCode, message, contextData);
    }

    public static InvalidSupplierPaymentOperationException emptyField(String fieldName) {
        return new InvalidSupplierPaymentOperationException(
                fieldName + " không được để trống.",
                "SUPPLIER_PAYMENT_EMPTY_FIELD",
                Map.of("fieldName", fieldName)
        );
    }

    public static InvalidSupplierPaymentOperationException invalidAmount(double amount) {
        return new InvalidSupplierPaymentOperationException(
                "Số tiền thanh toán phải lớn hơn 0.",
                "SUPPLIER_PAYMENT_INVALID_AMOUNT",
                Map.of("amount", amount)
        );
    }

    public static InvalidSupplierPaymentOperationException paymentExceedsRemaining(Integer invoiceId, double amount, double remainingAmount) {
        return new InvalidSupplierPaymentOperationException(
                "Số tiền chi trả vượt quá số tiền còn phải thanh toán.",
                "SUPPLIER_PAYMENT_EXCEEDS_REMAINING",
                Map.of("invoiceId", invoiceId, "amount", amount, "remainingAmount", remainingAmount)
        );
    }

    public static InvalidSupplierPaymentOperationException closedInvoice(Integer invoiceId, SupplierPaymentStatus status) {
        return new InvalidSupplierPaymentOperationException(
                "Khoản thanh toán đã đóng, không thể ghi nhận thêm chi trả.",
                "SUPPLIER_PAYMENT_CLOSED",
                Map.of("invoiceId", invoiceId, "status", status.getCode())
        );
    }

    public static InvalidSupplierPaymentOperationException invalidTransition(SupplierPaymentStatus currentStatus, SupplierPaymentStatus targetStatus) {
        return new InvalidSupplierPaymentOperationException(
                "Không thể chuyển trạng thái thanh toán nhà cung cấp theo luồng hiện tại.",
                "SUPPLIER_PAYMENT_INVALID_TRANSITION",
                Map.of("currentStatus", currentStatus.getCode(), "targetStatus", targetStatus.getCode())
        );
    }

    public static InvalidSupplierPaymentOperationException importNoteNotCompleted(Integer importNoteId, String status) {
        return new InvalidSupplierPaymentOperationException(
                "Chỉ phiếu nhập đã hoàn tất mới được tạo công nợ nhà cung cấp.",
                "SUPPLIER_PAYMENT_IMPORT_NOT_COMPLETED",
                Map.of("importNoteId", importNoteId, "status", status)
        );
    }

    public static InvalidSupplierPaymentOperationException invalidStatus(String invalidValue) {
        return new InvalidSupplierPaymentOperationException(
                "Trạng thái thanh toán nhà cung cấp không hợp lệ.",
                "SUPPLIER_PAYMENT_INVALID_STATUS",
                Map.of("invalidValue", invalidValue, "allowedValues", Arrays.stream(SupplierPaymentStatus.values()).map(SupplierPaymentStatus::getCode).toList())
        );
    }

    public static InvalidSupplierPaymentOperationException invalidMethod(String invalidValue) {
        return new InvalidSupplierPaymentOperationException(
                "Phương thức thanh toán nhà cung cấp không hợp lệ.",
                "SUPPLIER_PAYMENT_INVALID_METHOD",
                Map.of("invalidValue", invalidValue, "allowedValues", Arrays.stream(SupplierPaymentMethod.values()).map(SupplierPaymentMethod::getCode).toList())
        );
    }
}
