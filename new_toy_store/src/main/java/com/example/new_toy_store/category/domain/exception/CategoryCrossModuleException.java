package com.example.new_toy_store.category.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.Set;

public class CategoryCrossModuleException extends CategoryDomainException {

    private CategoryCrossModuleException(String message, String sourceModule, Object invalidValue) {
        super(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "CATEGORY_CROSS_MODULE_INVALID_DATA",
                message,
                Map.of(
                        "sourceModule", sourceModule,
                        "invalidValue", invalidValue == null ? "" : invalidValue,
                        "reason", "INVALID_CROSS_MODULE_REFERENCE"
                )
        );
    }

    public static CategoryCrossModuleException missingCategories(String sourceModule, Set<Integer> missingCategoryIds) {
        return new CategoryCrossModuleException(
                "Dữ liệu từ module " + sourceModule + " gửi sang chứa danh mục không tồn tại hoặc đã bị xóa: " + missingCategoryIds + ".",
                sourceModule,
                missingCategoryIds
        );
    }
}
