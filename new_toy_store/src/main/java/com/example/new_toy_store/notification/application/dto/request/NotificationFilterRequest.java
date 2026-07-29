package com.example.new_toy_store.notification.application.dto.request;

import com.example.new_toy_store.notification.domain.NotificationStatus;
import com.example.new_toy_store.notification.domain.NotificationType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class NotificationFilterRequest {

    private NotificationStatus status;
    private NotificationType type;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;

    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public LocalDateTime getFromDate() { return fromDate; }
    public void setFromDate(LocalDateTime fromDate) { this.fromDate = fromDate; }
    public LocalDateTime getToDate() { return toDate; }
    public void setToDate(LocalDateTime toDate) { this.toDate = toDate; }
}
