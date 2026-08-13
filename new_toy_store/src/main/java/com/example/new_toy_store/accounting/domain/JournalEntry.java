package com.example.new_toy_store.accounting.domain;

import com.example.new_toy_store.accounting.domain.exception.InvalidJournalEntryException;
import com.example.new_toy_store.global.common.BaseRootEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(name = "journal_entries", uniqueConstraints = {
        @UniqueConstraint(name = "uk_journal_entry_number", columnNames = "entry_number"),
        @UniqueConstraint(name = "uk_journal_source", columnNames = {"source_type", "source_reference"})
}, indexes = {
        @Index(name = "idx_journal_entry_date", columnList = "entry_date"),
        @Index(name = "idx_journal_entry_source", columnList = "source_type,source_reference"),
        @Index(name = "idx_journal_entry_status", columnList = "status")
})
public class JournalEntry extends BaseRootEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "entry_number", nullable = false, length = 50)
    private String entryNumber;

    @Column(name = "entry_date", nullable = false)
    private LocalDate entryDate;

    @Column(nullable = false, length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    private AccountingSourceType sourceType;

    @Column(name = "source_reference", nullable = false, length = 100)
    private String sourceReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JournalEntryStatus status = JournalEntryStatus.POSTED;

    @Column(name = "posted_by", length = 150)
    private String postedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversed_entry_id")
    private JournalEntry reversedEntry;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JournalEntryLine> lines = new ArrayList<>();

    protected JournalEntry() {}

    public JournalEntry(String entryNumber, LocalDate entryDate, String description,
                        AccountingSourceType sourceType, String sourceReference, String postedBy) {
        this.entryNumber = entryNumber;
        this.entryDate = entryDate == null ? LocalDate.now() : entryDate;
        this.description = description;
        this.sourceType = sourceType;
        this.sourceReference = sourceReference;
        this.postedBy = postedBy;
    }

    public void addLine(JournalEntryLine line) {
        line.attachTo(this);
        lines.add(line);
    }

    public void validateBalanced() {
        if (lines.size() < 2) {
            throw new InvalidJournalEntryException("Bút toán phải có ít nhất hai dòng.", java.util.Map.of("lineCount", lines.size()));
        }
        double debit = getTotalDebit();
        double credit = getTotalCredit();
        if (Math.abs(debit - credit) >= 0.01) {
            throw InvalidJournalEntryException.unbalanced(debit, credit);
        }
    }

    public void markReversed() { this.status = JournalEntryStatus.REVERSED; }
    public void reverseEntryOf(JournalEntry original) { this.reversedEntry = original; }

    public double getTotalDebit() { return round(lines.stream().mapToDouble(JournalEntryLine::getDebitAmount).sum()); }
    public double getTotalCredit() { return round(lines.stream().mapToDouble(JournalEntryLine::getCreditAmount).sum()); }
    private double round(double value) { return Math.round(value * 100.0) / 100.0; }

    public Integer getId() { return id; }
    public String getEntryNumber() { return entryNumber; }
    public LocalDate getEntryDate() { return entryDate; }
    public String getDescription() { return description; }
    public AccountingSourceType getSourceType() { return sourceType; }
    public String getSourceReference() { return sourceReference; }
    public JournalEntryStatus getStatus() { return status; }
    public String getPostedBy() { return postedBy; }
    public JournalEntry getReversedEntry() { return reversedEntry; }
    public List<JournalEntryLine> getLines() { return Collections.unmodifiableList(lines); }
}
