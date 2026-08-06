package com.example.new_toy_store.customer_return.domain.converter;

import com.example.new_toy_store.customer_return.domain.CustomerReturnStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CustomerReturnStatusConverter implements AttributeConverter<CustomerReturnStatus, String> {

    @Override
    public String convertToDatabaseColumn(CustomerReturnStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public CustomerReturnStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }
        return CustomerReturnStatus.from(dbData);
    }
}
