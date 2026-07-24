package com.example.new_toy_store.imports.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class DuplicateActiveImportNoteException extends ImportDomainException {

    public DuplicateActiveImportNoteException(Integer supplierId, String businessKey) {
        super(
                HttpStatus.CONFLICT,
                "DUPLICATE_ACTIVE_IMPORT_NOTE",
                "Đã tồn tại phiếu nhập đang hoạt động cho nhà cung cấp ID " + supplierId + " với khóa nghiệp vụ " + businessKey + ".",
                Map.of(
                        "supplierId", supplierId,
                        "businessKey", businessKey,
                        "reason", "DUPLICATE_ACTIVE_DATA"
                )
        );
    }
}
