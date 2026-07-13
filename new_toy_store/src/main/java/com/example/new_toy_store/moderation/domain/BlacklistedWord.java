package com.example.new_toy_store.moderation.domain;

import com.example.new_toy_store.global.common.BaseRootEntity;
import com.example.new_toy_store.moderation.domain.exception.InvalidModerationOperationException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "blacklisted_words",
        indexes = {
                @Index(name = "idx_blacklisted_word", columnList = "word", unique = true),
                @Index(name = "idx_blacklisted_category", columnList = "category")
        }
)
public class BlacklistedWord extends BaseRootEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String word;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WordCategory category;

    protected BlacklistedWord() {}

    public BlacklistedWord(String word, WordCategory category) {
        validateWord(word);
        if (category == null) throw InvalidModerationOperationException.nullCategory();
        this.word = word.trim().toLowerCase();
        this.category = category;
    }

    public void updateWord(String newWord, WordCategory newCategory) {
        validateWord(newWord);
        if (newCategory == null) throw InvalidModerationOperationException.nullCategory();
        this.word = newWord.trim().toLowerCase();
        this.category = newCategory;
    }

    private void validateWord(String word) {
        if (word == null || word.trim().isEmpty()) throw InvalidModerationOperationException.emptyWord();
    }

    public Integer getId() { return id; }
    public String getWord() { return word; }
    public WordCategory getCategory() { return category; }

    @Override public boolean equals(Object o) { return this == o || (o instanceof BlacklistedWord p && id != null && id.equals(p.id)); }
    @Override public int hashCode() { return getClass().hashCode(); }
}