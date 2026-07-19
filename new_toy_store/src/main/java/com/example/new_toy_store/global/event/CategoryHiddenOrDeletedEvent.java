package com.example.new_toy_store.global.event;

public class CategoryHiddenOrDeletedEvent {
    private final Integer categoryId;
    private final String action;

    public CategoryHiddenOrDeletedEvent(Integer categoryId, String action) {
        this.categoryId = categoryId;
        this.action = action;
    }

    public Integer getCategoryId() { return categoryId; }
    public String getAction() { return action; }
}