package com.example.new_toy_store.cart.domain.exception;

public class InvalidCartDataException extends CartDomainException {

    private final String field;
    private final Object invalidValue;

    private InvalidCartDataException(String message, String field, Object invalidValue) {
        super(message, "INVALID_CART_DATA");
        this.field = field;
        this.invalidValue = invalidValue;
        addContext("field", field);
        addContext("invalidValue", invalidValue);
    }

    public static InvalidCartDataException emptyStatus() {
        return new InvalidCartDataException(
                "Trạng thái giỏ hàng không được để trống.",
                "STATUS",
                null
        );
    }

    public static InvalidCartDataException invalidStatus(String value) {
        return new InvalidCartDataException(
                "Trạng thái giỏ hàng không hợp lệ: " + value,
                "STATUS",
                value
        );
    }

    public static InvalidCartDataException invalidField(String fieldName, Object value, String reason) {
        return new InvalidCartDataException(
                "Dữ liệu " + fieldName + " không hợp lệ: " + reason,
                fieldName,
                value
        );
    }

    public String getField() { return field; }
    public Object getInvalidValue() { return invalidValue; }
}
