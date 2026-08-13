package com.example.new_toy_store.accounting.domain;

import com.example.new_toy_store.accounting.domain.exception.InvalidJournalEntryException;
import com.example.new_toy_store.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "journal_entry_lines", indexes = {
        @Index(name = "idx_journal_line_entry", columnList = "journal_entry_id"),
        @Index(name = "idx_journal_line_account", columnList = "account_id")
})
public class JournalEntryLine extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private LedgerAccount account;

    @Column(length = 300)
    private String description;

    @Column(name = "debit_amount", nullable = false)
    private double debitAmount;

    @Column(name = "credit_amount", nullable = false)
    private double creditAmount;

    protected JournalEntryLine() {}

    public JournalEntryLine(LedgerAccount account, String description, double debitAmount, double creditAmount) {
        double debit = roundMoney(debitAmount);
        double credit = roundMoney(creditAmount);
        if ((debit <= 0 && credit <= 0) || (debit > 0 && credit > 0)) {
            throw InvalidJournalEntryException.invalidLine(debit, credit);
        }
        this.account = account;
        this.description = description;
        this.debitAmount = debit;
        this.creditAmount = credit;
    }

    void attachTo(JournalEntry entry) { this.journalEntry = entry; }

    private double roundMoney(double value) { return Math.round(Math.max(0.0, value) * 100.0) / 100.0; }

    public Integer getId() { return id; }
    public JournalEntry getJournalEntry() { return journalEntry; }
    public LedgerAccount getAccount() { return account; }
    public String getDescription() { return description; }
    public double getDebitAmount() { return debitAmount; }
    public double getCreditAmount() { return creditAmount; }
}
