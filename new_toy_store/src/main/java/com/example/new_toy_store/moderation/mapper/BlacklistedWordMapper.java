package com.example.new_toy_store.moderation.mapper;

import com.example.new_toy_store.moderation.application.dto.response.BlacklistedWordResponse;
import com.example.new_toy_store.moderation.domain.BlacklistedWord;

public class BlacklistedWordMapper {

    public static BlacklistedWordResponse toResponse(BlacklistedWord entity) {
        if (entity == null) {
            return null;
        }

        return new BlacklistedWordResponse(
                entity.getId(),
                entity.getWord(),
                entity.getCategory().name(),
                entity.getCategory().getDescription(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}