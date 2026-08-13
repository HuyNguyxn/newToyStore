package com.example.new_toy_store.accounting.domain.exception;

import java.util.Map;

public class LedgerAccountNotFoundException extends AccountingDomainException {
    public LedgerAccountNotFoundException(String accountCode) {
        super("LEDGER_ACCOUNT_NOT_FOUND", "Không tìm thấy tài khoản kế toán " + accountCode + ".", Map.of("accountCode", accountCode));
    }
}
