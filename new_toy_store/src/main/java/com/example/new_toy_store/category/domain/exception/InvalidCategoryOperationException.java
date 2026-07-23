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

    public static InvalidCategoryOperationException maxDepthExceeded(int maxLevel) {
        return new InvalidCategoryOperationException(
                "Độ sâu danh mục vượt quá giới hạn cho phép (Tối đa " + maxLevel + " cấp).",
                "ASSIGN_PARENT",
                maxLevel
        );
    }

    public static InvalidCategoryOperationException emptyStatus() {
        return new InvalidCategoryOperationException(
                "Trạng thái danh mục không được để trống.",
                "PARSE_STATUS",
                null
        );
    }

    public static InvalidCategoryOperationException invalidStatus(String value) {
        return new InvalidCategoryOperationException(
                "Trạng thái danh mục không hợp lệ: " + value,
                "PARSE_STATUS",
                value
        );
    }

    public static InvalidCategoryOperationException parentIsHidden(Integer categoryId) {
        return new InvalidCategoryOperationException(
                "Không thể hiển thị danh mục này vì danh mục cha đang bị ẩn.",
                "SHOW_CATEGORY",
                categoryId
        );
    }

    public static InvalidCategoryOperationException invalidStatusTransition(String currentStatus, String targetStatus) {
        return new InvalidCategoryOperationException(
                "Không thể chuyển trạng thái danh mục từ " + currentStatus + " sang " + targetStatus + ".",
                "CHANGE_STATUS",
                targetStatus
        );
    }

    public String getOperation() { return operation; }
    public Object getInvalidValue() { return invalidValue; }
}
