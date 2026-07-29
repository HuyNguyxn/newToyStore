package com.example.new_toy_store.notification.domain.converter;

import com.example.new_toy_store.notification.domain.NotificationStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class NotificationStatusConverter implements AttributeConverter<NotificationStatus, String> {

    @Override
    public String convertToDatabaseColumn(NotificationStatus attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public NotificationStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : NotificationStatus.from(dbData);
    }
}
