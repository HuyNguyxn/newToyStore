package com.example.new_toy_store.category.domain.exception;

public class InvalidCategoryOperationException extends RuntimeException {

    private final String operation;
    private final Object invalidValue;

    private InvalidCategoryOperationException(String message, String operation, Object invalidValue) {
        super(message);
        this.operation = operation;
        this.invalidValue = invalidValue;
    }

    public static InvalidCategoryOperationException emptyNameOrSlug() {
        return new InvalidCategoryOperationException(
                "Tên danh mục và đường dẫn tĩnh không được để trống.",
                "CREATE_OR_UPDATE",
                null
        );
    }

    public static InvalidCategoryOperationException selfParenting(Integer categoryId) {
        String target = categoryId != null ? " (ID: " + categoryId + ")" : "";
        return new InvalidCategoryOperationException(
                "Danh mục" + target + " không thể tự nhận chính mình làm danh mục cha.",
                "ASSIGN_PARENT",
                categoryId
        );
    }

    public static InvalidCategoryOperationException circularReference(Integer categoryId, Integer parentId) {
        String target = categoryId != null ? " (ID: " + categoryId + ")" : "";
        return new InvalidCategoryOperationException(
                "Phát hiện lỗi vòng lặp: Danh mục" + target + " hiện đang là cấp trên của danh mục mục tiêu (ID: " + parentId + ").",
                "ASSIGN_PARENT",
                parentId
        );
    }

    public String getOperation() { return operation; }
    public Object getInvalidValue() { return invalidValue; }
}