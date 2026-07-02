package com.example.new_toy_store.category.domain.exception;

public class DuplicateCategorySlugException extends RuntimeException {

    private final String slug;

    public DuplicateCategorySlugException(String slug) {
        super("Đường dẫn tĩnh '" + slug + "' đã tồn tại trong hệ thống. Vui lòng chọn một đường dẫn khác.");
        this.slug = slug;
    }

    public String getSlug() { return slug; }
}