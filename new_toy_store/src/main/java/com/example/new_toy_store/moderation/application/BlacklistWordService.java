package com.example.new_toy_store.moderation.application;

import com.example.new_toy_store.infrastructure.specification.BlacklistedWordSpecification;
import com.example.new_toy_store.moderation.application.dto.request.BlacklistedWordFilterRequest;
import com.example.new_toy_store.moderation.application.dto.response.BlacklistedWordResponse;
import com.example.new_toy_store.moderation.domain.BlacklistedWord;
import com.example.new_toy_store.moderation.domain.BlacklistedWordRepository;
import com.example.new_toy_store.moderation.domain.WordCategory;
import com.example.new_toy_store.moderation.domain.exception.BlacklistedWordNotFoundException;
import com.example.new_toy_store.moderation.domain.exception.ModerationConflictException;
import com.example.new_toy_store.moderation.mapper.BlacklistedWordMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class BlacklistWordService {

    private final BlacklistedWordRepository repository;
    private final Set<String> cachedBadWords = ConcurrentHashMap.newKeySet();

    public BlacklistWordService(BlacklistedWordRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void loadCacheFromDatabase() {
        cachedBadWords.clear();
        cachedBadWords.addAll(repository.findAll().stream()
                .map(BlacklistedWord::getWord)
                .collect(Collectors.toSet()));
    }

    public boolean containsBadWord(String text) {
        if (text == null || text.trim().isEmpty()) return false;
        String lowerText = text.toLowerCase();

        for (String badWord : cachedBadWords) {
            String regex = ".*\\b" + badWord + "\\b.*";
            if (lowerText.matches(regex)) {
                return true;
            }
        }
        return false;
    }

    @Transactional(readOnly = true)
    public Page<BlacklistedWordResponse> searchWords(BlacklistedWordFilterRequest filter, Pageable pageable) {
        Page<BlacklistedWord> entities = repository.findAll(BlacklistedWordSpecification.filter(filter), pageable);
        return entities.map(BlacklistedWordMapper::toResponse);
    }

    @Transactional
    public void addWord(String word, WordCategory category) {
        String cleanWord = word.trim().toLowerCase();

        Optional<BlacklistedWord> existingWord = repository.findByWordIncludingDeleted(cleanWord);
        if (existingWord.isPresent()) {
            if (existingWord.get().isDeleted()) {
                throw ModerationConflictException.deletedWordConflict(cleanWord);
            }
            throw ModerationConflictException.duplicateWord(cleanWord);
        }

        repository.save(new BlacklistedWord(cleanWord, category));
        cachedBadWords.add(cleanWord);
    }

    @Transactional
    public void updateWord(Integer id, String newWord, WordCategory newCategory) {
        BlacklistedWord entity = repository.findById(id)
                .orElseThrow(() -> new BlacklistedWordNotFoundException(id));

        String cleanNewWord = newWord.trim().toLowerCase();

        Optional<BlacklistedWord> existingWord = repository.findByWordIncludingDeleted(cleanNewWord);
        if (existingWord.isPresent() && !existingWord.get().getId().equals(id)) {
            throw ModerationConflictException.duplicateWord(cleanNewWord);
        }

        String oldWord = entity.getWord();
        entity.updateWord(cleanNewWord, newCategory);
        repository.save(entity);

        cachedBadWords.remove(oldWord);
        cachedBadWords.add(entity.getWord());
    }

    @Transactional
    public void softDeleteWord(Integer id) {
        BlacklistedWord entity = repository.findById(id)
                .orElseThrow(() -> new BlacklistedWordNotFoundException(id));
        entity.delete();
        repository.save(entity);
        cachedBadWords.remove(entity.getWord());
    }

    @Transactional
    public void restoreWord(Integer id) {
        BlacklistedWord entity = repository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new BlacklistedWordNotFoundException(id));
        entity.restore();
        repository.save(entity);
        cachedBadWords.add(entity.getWord());
    }

    @Transactional
    public void hardDeleteWord(Integer id) {
        BlacklistedWord entity = repository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new BlacklistedWordNotFoundException(id));
        repository.delete(entity);
        cachedBadWords.remove(entity.getWord());
    }
}