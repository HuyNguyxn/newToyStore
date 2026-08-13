package com.example.new_toy_store.accounting.domain;

import com.example.new_toy_store.global.common.BaseRootEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.SQLRestriction;

@Entity
@SQLRestriction("deleted_at IS NULL")
@Table(
        name = "ledger_accounts",
        uniqueConstraints = @UniqueConstraint(name = "uk_ledger_account_code", columnNames = "code"),
        indexes = @Index(name = "idx_ledger_account_type_active", columnList = "account_type,active")
)
public class LedgerAccount extends BaseRootEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 30)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "normal_balance", nullable = false, length = 10)
    private NormalBalance normalBalance;

    @Column(name = "liquid_account", nullable = false)
    private boolean liquidAccount;

    @Column(name = "system_account", nullable = false)
    private boolean systemAccount;

    @Column(nullable = false)
    private boolean active = true;

    @Column(length = 500)
    private String description;

    protected LedgerAccount() {}

    public Integer getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public AccountType getAccountType() { return accountType; }
    public NormalBalance getNormalBalance() { return normalBalance; }
    public boolean isLiquidAccount() { return liquidAccount; }
    public boolean isSystemAccount() { return systemAccount; }
    public boolean isActive() { return active; }
    public String getDescription() { return description; }

    public double calculateBalance(double totalDebit, double totalCredit) {
        double balance = normalBalance == NormalBalance.DEBIT
                ? totalDebit - totalCredit
                : totalCredit - totalDebit;
        return Math.round(balance * 100.0) / 100.0;
    }
}
