package com.example.new_toy_store.moderation.application.facade;

import com.example.new_toy_store.moderation.application.BlacklistWordService;
import org.springframework.stereotype.Component;

@Component
public class ModerationFacade {

    private final BlacklistWordService blacklistWordService;

    public ModerationFacade(BlacklistWordService blacklistWordService) {
        this.blacklistWordService = blacklistWordService;
    }

    public boolean containsProhibitedWord(String text) {
        return blacklistWordService.containsBadWord(text);
    }
}
