package com.example.new_toy_store.notification.domain;

import com.example.new_toy_store.notification.domain.exception.InvalidNotificationOperationException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum NotificationType {
    ORDER_CREATED("Tạo đơn hàng", NotificationCategory.ORDER, false),
    ORDER_STATUS_CHANGED("Cập nhật đơn hàng", NotificationCategory.ORDER, true),
    ORDER_CANCELLED("Hủy đơn hàng", NotificationCategory.ORDER, true),
    PAYMENT_COMPLETED("Thanh toán thành công", NotificationCategory.PAYMENT, true),
    PAYMENT_FAILED("Thanh toán thất bại", NotificationCategory.PAYMENT, true),
    PAYMENT_REFUNDED("Hoàn tiền thanh toán", NotificationCategory.PAYMENT, true),
    SHIPMENT_CREATED("Tạo đơn vận chuyển", NotificationCategory.SHIPMENT, false),
    SHIPMENT_IN_TRANSIT("Đang vận chuyển", NotificationCategory.SHIPMENT, false),
    SHIPMENT_DELIVERED("Giao hàng thành công", NotificationCategory.SHIPMENT, true),
    SHIPMENT_RETURNED("Trả hàng thành công", NotificationCategory.SHIPMENT, true),
    SHIPMENT_CANCELLED("Hủy đơn vận chuyển", NotificationCategory.SHIPMENT, true),
    RETURN_STATUS_CHANGED("Cập nhật yêu cầu trả hàng", NotificationCategory.RETURN, true),
    RETURN_REFUNDED("Hoàn tiền trả hàng", NotificationCategory.RETURN, true),
    REVIEW_REPLIED("Phản hồi đánh giá", NotificationCategory.REVIEW, false),
    REVIEW_STATUS_CHANGED("Cập nhật đánh giá", NotificationCategory.REVIEW, false),
    CART_EXPIRING("Giỏ hàng", NotificationCategory.CART, false),
    SYSTEM_ANNOUNCEMENT("Thông báo hệ thống", NotificationCategory.SYSTEM, true);

    private final String displayName;
    private final NotificationCategory category;
    private final boolean emailSupported;

    NotificationType(String displayName, NotificationCategory category, boolean emailSupported) {
        this.displayName = displayName;
        this.category = category;
        this.emailSupported = emailSupported;
    }

    public String getCode() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public boolean isEmailSupported() {
        return emailSupported;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static NotificationType from(String value) {
        if (value == null || value.isBlank()) {
            throw InvalidNotificationOperationException.invalidType(value);
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw InvalidNotificationOperationException.invalidType(value);
        }
    }
}
