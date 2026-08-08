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

    public static Specification<ImportNote> filter(Integer supplierId, ImportStatus status, String keyword) {
        return filter(supplierId, status).and(matchesKeyword(keyword));
    }

    public static Specification<ImportNote> hasSupplierId(Integer supplierId) {
        return BaseSpecification.isEqual("supplierId", supplierId);
    }

    public static Specification<ImportNote> hasStatus(ImportStatus status) {
        return BaseSpecification.isEqual("status", status);
    }

    /** Searches the import-note number (PN000001 or 1) and the operator note. */
    public static Specification<ImportNote> matchesKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return Specification.where(null);
        }
        String value = keyword.trim();
        String number = value.replaceFirst("(?i)^PN", "").replaceFirst("^0+", "");
        Integer id = null;
        try {
            if (!number.isBlank()) id = Integer.valueOf(number);
        } catch (NumberFormatException ignored) {
            // A non-numeric keyword can still match the operator note.
        }
        Integer noteId = id;
        return (root, query, cb) -> {
            var noteMatch = cb.like(cb.lower(root.get("note")), "%" + value.toLowerCase(java.util.Locale.ROOT) + "%");
            return noteId == null ? noteMatch : cb.or(cb.equal(root.get("id"), noteId), noteMatch);
        };
    }
}
