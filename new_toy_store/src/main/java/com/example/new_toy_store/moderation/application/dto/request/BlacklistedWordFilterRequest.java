package com.example.new_toy_store.moderation.application.dto.request;

import jakarta.validation.constraints.Size;

public class BlacklistedWordFilterRequest {
    @Size(max = 100, message = "Từ khóa tìm kiếm không quá 100 ký tự")
    private String keyword;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
}