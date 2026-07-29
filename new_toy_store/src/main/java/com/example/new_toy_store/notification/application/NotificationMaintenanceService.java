package com.example.new_toy_store.notification.application;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class NotificationMaintenanceService {

    private final NotificationService notificationService;

    public NotificationMaintenanceService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "${app.notification.cleanup.cron:0 30 2 * * ?}", zone = "${app.notification.zone:Asia/Ho_Chi_Minh}")
    public void cleanupExpiredNotifications() {
        notificationService.cleanupExpired();
    }
}
