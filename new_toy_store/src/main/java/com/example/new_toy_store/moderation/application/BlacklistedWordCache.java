package com.example.new_toy_store.moderation.application;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BlacklistedWordCache {

    private final Set<String> cachedBadWords = ConcurrentHashMap.newKeySet();

    public void reload(Collection<String> words) {
        cachedBadWords.clear();
        cachedBadWords.addAll(words);
    }

    public boolean containsBadWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        String lowerText = text.toLowerCase();
        for (String badWord : cachedBadWords) {
            String regex = ".*\\b" + badWord + "\\b.*";
            if (lowerText.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    public void add(String word) {
        cachedBadWords.add(word);
    }

    public void replace(String oldWord, String newWord) {
        cachedBadWords.remove(oldWord);
        cachedBadWords.add(newWord);
    }

    public void remove(String word) {
        cachedBadWords.remove(word);
    }
}
