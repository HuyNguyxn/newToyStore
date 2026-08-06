package com.example.new_toy_store.logistics.domain.converter;

import com.example.new_toy_store.logistics.domain.ShipmentStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ShipmentStatusConverter implements AttributeConverter<ShipmentStatus, String> {

    @Override
    public String convertToDatabaseColumn(ShipmentStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public ShipmentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return ShipmentStatus.from(dbData);
    }
}
