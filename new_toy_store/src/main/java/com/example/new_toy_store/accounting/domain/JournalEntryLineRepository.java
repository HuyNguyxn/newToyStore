package com.example.new_toy_store.accounting.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Integer> {
    @Query("""
            SELECT COALESCE(SUM(l.debitAmount - l.creditAmount), 0)
              FROM JournalEntryLine l
             WHERE l.account.liquidAccount = true
               AND l.journalEntry.entryDate <= :asOf
            """)
    double calculateLiquidFundsAsOf(@Param("asOf") LocalDate asOf);

    @Query("""
            SELECT l.account.id, COALESCE(SUM(l.debitAmount), 0), COALESCE(SUM(l.creditAmount), 0)
              FROM JournalEntryLine l
             WHERE l.journalEntry.entryDate <= :asOf
             GROUP BY l.account.id
            """)
    List<Object[]> summarizeBalancesAsOf(@Param("asOf") LocalDate asOf);

    @Query("""
            SELECT l.account.id, COALESCE(SUM(l.debitAmount), 0), COALESCE(SUM(l.creditAmount), 0)
              FROM JournalEntryLine l
             WHERE l.journalEntry.entryDate >= :from
               AND l.journalEntry.entryDate <= :to
             GROUP BY l.account.id
            """)
    List<Object[]> summarizeBalancesBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @EntityGraph(attributePaths = {"journalEntry", "account"})
    @Query("""
            SELECT l FROM JournalEntryLine l
             WHERE l.account.code = :accountCode
               AND l.journalEntry.entryDate >= :from
               AND l.journalEntry.entryDate <= :to
             ORDER BY l.journalEntry.entryDate DESC, l.id DESC
            """)
    Page<JournalEntryLine> findGeneralLedger(
            @Param("accountCode") String accountCode,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable
    );
}
