package com.example.new_toy_store.category.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateCategorySlugException extends CategoryDomainException {

    private final String slug;

    public DuplicateCategorySlugException(String slug) {
        super(
                HttpStatus.CONFLICT,
                "CATEGORY_ACTIVE_SLUG_DUPLICATE",
                "Đường dẫn tĩnh '" + slug + "' đã được sử dụng bởi một danh mục đang hoạt động.",
                Map.of(
                        "slug", slug,
                        "conflictType", "ACTIVE",
                        "suggestedAction", "USE_DIFFERENT_SLUG"
                )
        );
        this.slug = slug;
    }

    public String getSlug() { return slug; }
}
