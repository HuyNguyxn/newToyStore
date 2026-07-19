package com.example.new_toy_store.global.event;

public class CategoryUpdatedEvent {
    private final Integer categoryId;
    private final String oldPath;
    private final String newPath;
    private final boolean pathChanged;

    public CategoryUpdatedEvent(Integer categoryId, String oldPath, String newPath, boolean pathChanged) {
        this.categoryId = categoryId;
        this.oldPath = oldPath;
        this.newPath = newPath;
        this.pathChanged = pathChanged;
    }

    public Integer getCategoryId() { return categoryId; }
    public String getOldPath() { return oldPath; }
    public String getNewPath() { return newPath; }
    public boolean isPathChanged() { return pathChanged; }
}