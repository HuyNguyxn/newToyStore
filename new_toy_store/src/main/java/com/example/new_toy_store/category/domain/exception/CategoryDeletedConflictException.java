package com.example.new_toy_store.category.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class CategoryDeletedConflictException extends CategoryDomainException {

    public CategoryDeletedConflictException(Integer categoryId) {
        super(
                HttpStatus.CONFLICT,
                "CATEGORY_DELETED_CONFLICT",
                "Danh mục có ID " + categoryId + " đã bị xóa mềm nên không thể tiếp tục thao tác.",
                Map.of(
                        "categoryId", categoryId,
                        "conflictType", "SOFT_DELETED",
                        "suggestedAction", "REFRESH_CATEGORY_DATA"
                )
        );
    }
}
