package com.example.new_toy_store.moderation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BlacklistedWordRepository extends JpaRepository<BlacklistedWord, Integer>, JpaSpecificationExecutor<BlacklistedWord> {

    @Query(value = "SELECT * FROM blacklisted_words WHERE id = :id", nativeQuery = true)
    Optional<BlacklistedWord> findByIdIncludingDeleted(@Param("id") Integer id);

    @Query(value = "SELECT * FROM blacklisted_words WHERE word = :word", nativeQuery = true)
    Optional<BlacklistedWord> findByWordIncludingDeleted(@Param("word") String word);
}