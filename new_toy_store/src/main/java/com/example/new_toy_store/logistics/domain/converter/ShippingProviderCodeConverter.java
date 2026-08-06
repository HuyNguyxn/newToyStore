package com.example.new_toy_store.logistics.domain.converter;

import com.example.new_toy_store.logistics.domain.ShippingProviderCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ShippingProviderCodeConverter implements AttributeConverter<ShippingProviderCode, String> {

    @Override
    public String convertToDatabaseColumn(ShippingProviderCode attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public ShippingProviderCode convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return ShippingProviderCode.from(dbData);
    }
}
