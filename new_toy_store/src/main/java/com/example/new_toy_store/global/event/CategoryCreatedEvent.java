package com.example.new_toy_store.global.event;

public class CategoryCreatedEvent {
    private final Integer categoryId;
    private final String slug;
    private final String path;

    public CategoryCreatedEvent(Integer categoryId, String slug, String path) {
        this.categoryId = categoryId;
        this.slug = slug;
        this.path = path;
    }

    public Integer getCategoryId() { return categoryId; }
    public String getSlug() { return slug; }
    public String getPath() { return path; }
}