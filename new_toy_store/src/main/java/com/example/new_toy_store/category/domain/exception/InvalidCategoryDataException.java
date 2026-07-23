package com.example.new_toy_store.category.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidCategoryDataException extends CategoryDomainException {

    private final String field;
    private final Object invalidValue;

    private InvalidCategoryDataException(String message, String field, Object invalidValue) {
        super(
                HttpStatus.BAD_REQUEST,
                "CATEGORY_INVALID_INPUT",
                message,
                Map.of(
                        "field", field,
                        "invalidValue", invalidValue == null ? "" : invalidValue,
                        "reason", "INVALID_INPUT"
                )
        );
        this.field = field;
        this.invalidValue = invalidValue;
    }

    public static InvalidCategoryDataException emptyStatus() {
        return new InvalidCategoryDataException(
                "Trạng thái danh mục không được để trống.",
                "STATUS",
                null
        );
    }

    public static InvalidCategoryDataException invalidStatus(String value) {
        return new InvalidCategoryDataException(
                "Trạng thái danh mục không hợp lệ: " + value + ".",
                "STATUS",
                value
        );
    }

    public static InvalidCategoryDataException emptyField(String fieldName) {
        return new InvalidCategoryDataException(
                "Trường dữ liệu " + fieldName + " không được để trống.",
                fieldName,
                null
        );
    }

    public String getField() { return field; }
    public Object getInvalidValue() { return invalidValue; }
}
