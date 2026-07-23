package com.example.new_toy_store.category.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class CategoryAccessDeniedException extends CategoryDomainException {

    private CategoryAccessDeniedException(String message, String action, Integer categoryId) {
        super(
                HttpStatus.FORBIDDEN,
                "CATEGORY_ACCESS_DENIED",
                message,
                Map.of(
                        "action", action,
                        "categoryId", categoryId == null ? "" : categoryId
                )
        );
    }

    public static CategoryAccessDeniedException cannotManage(Integer categoryId) {
        return new CategoryAccessDeniedException(
                "Bạn không có quyền quản lý danh mục này.",
                "MANAGE_CATEGORY",
                categoryId
        );
    }
}
