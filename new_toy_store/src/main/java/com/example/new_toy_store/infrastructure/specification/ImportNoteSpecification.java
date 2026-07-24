package com.example.new_toy_store.infrastructure.specification;

import com.example.new_toy_store.global.specification.BaseSpecification;
import com.example.new_toy_store.imports.domain.ImportNote;
import com.example.new_toy_store.imports.domain.ImportStatus;
import org.springframework.data.jpa.domain.Specification;

public final class ImportNoteSpecification {

    private ImportNoteSpecification() {
    }

    public static Specification<ImportNote> filter(Integer supplierId, ImportStatus status) {
        return Specification.where(hasSupplierId(supplierId))
                .and(hasStatus(status));
    }

    public static Specification<ImportNote> hasSupplierId(Integer supplierId) {
        return BaseSpecification.isEqual("supplierId", supplierId);
    }

    public static Specification<ImportNote> hasStatus(ImportStatus status) {
        return BaseSpecification.isEqual("status", status);
    }
}
