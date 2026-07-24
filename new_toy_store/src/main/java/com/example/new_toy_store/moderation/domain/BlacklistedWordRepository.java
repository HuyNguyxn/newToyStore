package com.example.new_toy_store.moderation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlacklistedWordRepository extends JpaRepository<BlacklistedWord, Integer>, JpaSpecificationExecutor<BlacklistedWord> {

    @Query(value = "SELECT * FROM blacklisted_words WHERE id = :id", nativeQuery = true)
    Optional<BlacklistedWord> findByIdIncludingDeleted(@Param("id") Integer id);

    @Query(value = "SELECT * FROM blacklisted_words WHERE word = :word", nativeQuery = true)
    Optional<BlacklistedWord> findByWordIncludingDeleted(@Param("word") String word);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update BlacklistedWord word
               set word.word = :newWord,
                   word.category = :category,
                   word.updatedAt = CURRENT_TIMESTAMP,
                   word.version = word.version + 1
             where word.id = :id
               and word.version = :version
               and word.deletedAt is null
            """)
    int updateWordWithVersion(@Param("id") Integer id,
                              @Param("version") Long version,
                              @Param("newWord") String newWord,
                              @Param("category") WordCategory category);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update BlacklistedWord word
               set word.deletedAt = CURRENT_TIMESTAMP,
                   word.updatedAt = CURRENT_TIMESTAMP,
                   word.version = word.version + 1
             where word.id = :id
               and word.version = :version
               and word.deletedAt is null
            """)
    int softDeleteWithVersion(@Param("id") Integer id, @Param("version") Long version);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE blacklisted_words
               SET deleted_at = NULL,
                   updated_at = CURRENT_TIMESTAMP,
                   version = version + 1
             WHERE id = :id
               AND version = :version
               AND deleted_at IS NOT NULL
            """, nativeQuery = true)
    int restoreWithVersion(@Param("id") Integer id, @Param("version") Long version);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM blacklisted_words WHERE id = :id AND version = :version", nativeQuery = true)
    int hardDeleteWithVersion(@Param("id") Integer id, @Param("version") Long version);
}
