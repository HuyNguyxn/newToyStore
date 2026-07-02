package com.example.new_toy_store.imports.domain.exception;

public class ImportNoteNotFoundException extends RuntimeException {
    public ImportNoteNotFoundException(Integer id) {
        super("Không tìm thấy phiếu nhập kho (ID: " + id + ").");
    }
}