package com.example.new_toy_store.imports.domain.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class InvalidImportDataException extends ImportDomainException {

    public InvalidImportDataException(String field, String message) {
        super(
                HttpStatus.BAD_REQUEST,
                "IMPORT_INVALID_INPUT",
                message,
                Map.of(
                        "field", field,
                        "reason", "INVALID_INPUT"
                )
        );
    }
}
