package com.example.new_toy_store.notification.domain.converter;

import com.example.new_toy_store.notification.domain.NotificationReferenceType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NotificationReferenceTypeConverter implements AttributeConverter<NotificationReferenceType, String> {

    @Override
    public String convertToDatabaseColumn(NotificationReferenceType attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public NotificationReferenceType convertToEntityAttribute(String dbData) {
        return dbData == null ? null : NotificationReferenceType.from(dbData);
    }
}
