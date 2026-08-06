package com.example.new_toy_store.logistics.domain.converter;

import com.example.new_toy_store.logistics.domain.ShipmentType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ShipmentTypeConverter implements AttributeConverter<ShipmentType, String> {

    @Override
    public String convertToDatabaseColumn(ShipmentType attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public ShipmentType convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return ShipmentType.from(dbData);
    }
}
