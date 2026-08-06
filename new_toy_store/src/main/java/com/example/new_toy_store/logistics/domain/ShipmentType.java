package com.example.new_toy_store.logistics.domain;

import com.example.new_toy_store.logistics.domain.exception.InvalidShipmentDataException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ShipmentType {

    FORWARD("FORWARD", "Giao hàng đi", "Giao đơn hàng bán lẻ từ Shop tới Khách hàng"),
    CUSTOMER_RETURN("CUSTOMER_RETURN", "Khách trả hàng hoàn về", "Khách hàng hoàn hàng lỗi về kho Shop"),
    SUPPLIER_RETURN("SUPPLIER_RETURN", "Xuất trả hàng NCC", "Shop trả hàng lỗi/hỏng về cho Nhà cung cấp");

    private final String code;
    private final String displayName;
    private final String description;

    ShipmentType(String code, String displayName, String description) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ShipmentType from(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidShipmentDataException("shipmentType", "Loại vận chuyển không được trống.");
        }
        try {
            return ShipmentType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidShipmentDataException("shipmentType", "Loại vận chuyển [" + value + "] không hợp lệ.");
        }
    }
}
