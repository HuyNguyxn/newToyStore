package com.example.new_toy_store.global.event;

public class CategoryStateChangedEvent {
    private final Integer categoryId;
    private final String state;

    public CategoryStateChangedEvent(Integer categoryId, String state) {
        this.categoryId = categoryId;
        this.state = state;
    }

    public Integer getCategoryId() { return categoryId; }
    public String getState() { return state; }
}