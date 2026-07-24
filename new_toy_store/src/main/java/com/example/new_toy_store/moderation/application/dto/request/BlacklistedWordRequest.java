package com.example.new_toy_store.moderation.application.dto.request;

import com.example.new_toy_store.moderation.domain.WordCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BlacklistedWordRequest {
    @NotBlank(message = "Từ khóa không được để trống")
    @Size(max = 100, message = "Từ khóa không được vượt quá 100 ký tự")
    private String word;

    @NotNull(message = "Loại từ khóa không được để trống")
    private WordCategory category;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public WordCategory getCategory() {
        return category;
    }

    public void setCategory(WordCategory category) {
        this.category = category;
    }
}
