package com.example.new_toy_store.moderation.application;

import com.example.new_toy_store.global.event.BlacklistedWordCreatedEvent;
import com.example.new_toy_store.global.event.BlacklistedWordDeletedEvent;
import com.example.new_toy_store.global.event.BlacklistedWordRestoredEvent;
import com.example.new_toy_store.global.event.BlacklistedWordUpdatedEvent;
import com.example.new_toy_store.infrastructure.specification.BlacklistedWordSpecification;
import com.example.new_toy_store.moderation.application.dto.request.BlacklistedWordFilterRequest;
import com.example.new_toy_store.moderation.application.dto.response.BlacklistedWordResponse;
import com.example.new_toy_store.moderation.domain.BlacklistedWord;
import com.example.new_toy_store.moderation.domain.BlacklistedWordRepository;
import com.example.new_toy_store.moderation.domain.WordCategory;
import com.example.new_toy_store.moderation.domain.exception.BlacklistedWordNotFoundException;
import com.example.new_toy_store.moderation.domain.exception.InvalidModerationOperationException;
import com.example.new_toy_store.moderation.domain.exception.ModerationConflictException;
import com.example.new_toy_store.moderation.mapper.BlacklistedWordMapper;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BlacklistWordService {

    private final BlacklistedWordRepository repository;
    private final BlacklistedWordCache cache;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;

    public BlacklistWordService(BlacklistedWordRepository repository,
                                BlacklistedWordCache cache,
                                ApplicationEventPublisher eventPublisher,
                                EntityManager entityManager) {
        this.repository = repository;
        this.cache = cache;
        this.eventPublisher = eventPublisher;
        this.entityManager = entityManager;
    }

    @PostConstruct
    public void loadCacheFromDatabase() {
        cache.reload(repository.findAll().stream()
                .map(BlacklistedWord::getWord)
                .collect(Collectors.toSet()));
    }

    public boolean containsBadWord(String text) {
        return cache.containsBadWord(text);
    }

    @Transactional(readOnly = true)
    public Page<BlacklistedWordResponse> searchWords(BlacklistedWordFilterRequest filter, Pageable pageable) {
        Page<BlacklistedWord> entities = repository.findAll(BlacklistedWordSpecification.filter(filter), pageable);
        return entities.map(BlacklistedWordMapper::toResponse);
    }

    @Transactional
    public void addWord(String word, WordCategory category) {
        String cleanWord = normalizeWord(word);

        Optional<BlacklistedWord> existingWord = repository.findByWordIncludingDeleted(cleanWord);
        if (existingWord.isPresent()) {
            if (existingWord.get().isDeleted()) {
                throw ModerationConflictException.deletedWordConflict(cleanWord);
            }
            throw ModerationConflictException.duplicateWord(cleanWord);
        }

        BlacklistedWord savedWord = repository.save(new BlacklistedWord(cleanWord, category));
        eventPublisher.publishEvent(BlacklistedWordCreatedEvent.now(savedWord.getId(), savedWord.getWord(), savedWord.getCategory()));
    }

    @Transactional
    public void updateWord(Integer id, String newWord, WordCategory newCategory) {
        BlacklistedWord entity = repository.findById(id)
                .orElseThrow(() -> new BlacklistedWordNotFoundException(id));

        String cleanNewWord = normalizeWord(newWord);

        Optional<BlacklistedWord> existingWord = repository.findByWordIncludingDeleted(cleanNewWord);
        if (existingWord.isPresent() && !existingWord.get().getId().equals(id)) {
            if (existingWord.get().isDeleted()) {
                throw ModerationConflictException.deletedWordConflict(cleanNewWord);
            }
            throw ModerationConflictException.duplicateWord(cleanNewWord);
        }

        String oldWord = entity.getWord();
        Long version = entity.getVersion();
        entity.updateWord(cleanNewWord, newCategory);
        entityManager.detach(entity);

        int updatedRows = repository.updateWordWithVersion(id, version, cleanNewWord, newCategory);
        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(BlacklistedWord.class, id);
        }

        eventPublisher.publishEvent(BlacklistedWordUpdatedEvent.now(id, oldWord, cleanNewWord, newCategory));
    }

    @Transactional
    public void softDeleteWord(Integer id) {
        BlacklistedWord entity = repository.findById(id)
                .orElseThrow(() -> new BlacklistedWordNotFoundException(id));

        entityManager.detach(entity);
        int updatedRows = repository.softDeleteWithVersion(id, entity.getVersion());
        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(BlacklistedWord.class, id);
        }

        eventPublisher.publishEvent(BlacklistedWordDeletedEvent.softDeleted(
                entity.getId(),
                entity.getWord(),
                entity.getCategory()
        ));
    }

    @Transactional
    public void restoreWord(Integer id) {
        BlacklistedWord entity = repository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new BlacklistedWordNotFoundException(id));

        entityManager.detach(entity);
        int updatedRows = repository.restoreWithVersion(id, entity.getVersion());
        if (updatedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(BlacklistedWord.class, id);
        }

        eventPublisher.publishEvent(BlacklistedWordRestoredEvent.now(
                entity.getId(),
                entity.getWord(),
                entity.getCategory()
        ));
    }

    @Transactional
    public void hardDeleteWord(Integer id) {
        BlacklistedWord entity = repository.findByIdIncludingDeleted(id)
                .orElseThrow(() -> new BlacklistedWordNotFoundException(id));

        entityManager.detach(entity);
        int deletedRows = repository.hardDeleteWithVersion(id, entity.getVersion());
        if (deletedRows == 0) {
            throw new ObjectOptimisticLockingFailureException(BlacklistedWord.class, id);
        }

        eventPublisher.publishEvent(BlacklistedWordDeletedEvent.hardDeleted(
                entity.getId(),
                entity.getWord(),
                entity.getCategory()
        ));
    }

    private String normalizeWord(String word) {
        if (word == null || word.trim().isEmpty()) {
            throw InvalidModerationOperationException.emptyWord();
        }
        return word.trim().toLowerCase();
    }
}
