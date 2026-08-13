package com.example.new_toy_store.accounting.domain.exception;

import java.util.Map;

public class InvalidJournalEntryException extends AccountingDomainException {
    public InvalidJournalEntryException(String message, Map<String, Object> context) {
        super("INVALID_JOURNAL_ENTRY", message, context);
    }

    public static InvalidJournalEntryException unbalanced(double debit, double credit) {
        return new InvalidJournalEntryException(
                "Bút toán không cân bằng: tổng Nợ phải bằng tổng Có.",
                Map.of("totalDebit", debit, "totalCredit", credit)
        );
    }

    public static InvalidJournalEntryException invalidLine(double debit, double credit) {
        return new InvalidJournalEntryException(
                "Mỗi dòng bút toán chỉ được ghi một bên Nợ hoặc Có và số tiền phải lớn hơn 0.",
                Map.of("debitAmount", debit, "creditAmount", credit)
        );
    }
}
