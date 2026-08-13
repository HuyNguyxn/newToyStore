package com.example.new_toy_store.accounting.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Integer> {
    boolean existsBySourceTypeAndSourceReference(AccountingSourceType sourceType, String sourceReference);

    @EntityGraph(attributePaths = {"lines", "lines.account", "reversedEntry"})
    Optional<JournalEntry> findBySourceTypeAndSourceReference(AccountingSourceType sourceType, String sourceReference);

    @EntityGraph(attributePaths = {"lines", "lines.account", "reversedEntry"})
    @Query("SELECT e FROM JournalEntry e WHERE e.id = :id")
    Optional<JournalEntry> findDetailsById(@Param("id") Integer id);

    @Override
    Page<JournalEntry> findAll(Pageable pageable);
}
