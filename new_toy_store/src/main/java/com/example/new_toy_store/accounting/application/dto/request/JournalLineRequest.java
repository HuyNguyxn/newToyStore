package com.example.new_toy_store.accounting.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class JournalLineRequest {
    @NotBlank
    @Size(max = 20)
    private String accountCode;
    @Size(max = 300)
    private String description;
    @PositiveOrZero
    private double debitAmount;
    @PositiveOrZero
    private double creditAmount;

    public String getAccountCode() { return accountCode; }
    public void setAccountCode(String accountCode) { this.accountCode = accountCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getDebitAmount() { return debitAmount; }
    public void setDebitAmount(double debitAmount) { this.debitAmount = debitAmount; }
    public double getCreditAmount() { return creditAmount; }
    public void setCreditAmount(double creditAmount) { this.creditAmount = creditAmount; }
}
