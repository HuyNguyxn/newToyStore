package com.example.new_toy_store.accounting.application.dto.request;

import com.example.new_toy_store.accounting.domain.AccountingSourceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class ManualJournalEntryRequest {
    private LocalDate entryDate;
    @NotBlank
    @Size(max = 500)
    private String description;
    @NotNull
    private AccountingSourceType sourceType = AccountingSourceType.MANUAL_ADJUSTMENT;
    @NotBlank
    @Size(max = 100)
    private String sourceReference;
    @Valid
    @NotEmpty
    private List<JournalLineRequest> lines;

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public AccountingSourceType getSourceType() { return sourceType; }
    public void setSourceType(AccountingSourceType sourceType) { this.sourceType = sourceType; }
    public String getSourceReference() { return sourceReference; }
    public void setSourceReference(String sourceReference) { this.sourceReference = sourceReference; }
    public List<JournalLineRequest> getLines() { return lines; }
    public void setLines(List<JournalLineRequest> lines) { this.lines = lines; }
}
