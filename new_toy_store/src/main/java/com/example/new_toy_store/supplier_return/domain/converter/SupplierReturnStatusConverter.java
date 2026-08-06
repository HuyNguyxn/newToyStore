package com.example.new_toy_store.supplier_return.domain.converter;

import com.example.new_toy_store.supplier_return.domain.SupplierReturnStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class SupplierReturnStatusConverter implements AttributeConverter<SupplierReturnStatus, String> {

    @Override
    public String convertToDatabaseColumn(SupplierReturnStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public SupplierReturnStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return SupplierReturnStatus.from(dbData);
    }
}
