package com.example.new_toy_store.moderation.mapper;

import com.example.new_toy_store.moderation.application.dto.response.BlacklistedWordActionResponse;
import com.example.new_toy_store.moderation.application.dto.response.BlacklistedWordResponse;
import com.example.new_toy_store.moderation.domain.BlacklistedWord;

import java.util.List;

public class BlacklistedWordMapper {

    private BlacklistedWordMapper() {
    }

    public static BlacklistedWordResponse toResponse(BlacklistedWord entity) {
        if (entity == null) {
            return null;
        }
        return toDetailResponse(entity);
    }

    public static BlacklistedWordResponse toDetailResponse(BlacklistedWord entity) {
        return createBlacklistedWordResponse(entity, mapAvailableActions(entity));
    }

    private static BlacklistedWordResponse createBlacklistedWordResponse(BlacklistedWord entity,
                                                                        List<BlacklistedWordActionResponse> actions) {
        return new BlacklistedWordResponse(
                entity.getId(),
                entity.getWord(),
                entity.getCategory(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                actions
        );
    }

    private static List<BlacklistedWordActionResponse> mapAvailableActions(BlacklistedWord entity) {
        if (entity.isDeleted()) {
            return List.of(
                    new BlacklistedWordActionResponse("RESTORE", "Khôi phục từ khóa"),
                    new BlacklistedWordActionResponse("HARD_DELETE", "Xóa vĩnh viễn")
            );
        }

        return List.of(
                new BlacklistedWordActionResponse("UPDATE", "Cập nhật từ khóa"),
                new BlacklistedWordActionResponse("SOFT_DELETE", "Xóa mềm")
        );
    }
}
