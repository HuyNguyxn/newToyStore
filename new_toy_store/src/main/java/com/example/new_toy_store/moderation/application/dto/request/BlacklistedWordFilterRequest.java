package com.example.new_toy_store.moderation.application.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BlacklistedWordFilterRequest {
    @Size(max = 100, message = "Từ khóa tìm kiếm không quá 100 ký tự")
    private String keyword;

    @Pattern(
            regexp = "^(PROFANITY|SPAM|COMPETITOR|OTHER)$",
            message = "Loại từ khóa chỉ chấp nhận PROFANITY, SPAM, COMPETITOR hoặc OTHER"
    )
    private String category;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
