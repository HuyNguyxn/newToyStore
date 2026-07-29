package com.example.new_toy_store.notification.api;

import com.example.new_toy_store.notification.application.NotificationFacade;
import com.example.new_toy_store.notification.application.dto.request.BroadcastNotificationRequest;
import com.example.new_toy_store.notification.application.dto.request.NotificationFilterRequest;
import com.example.new_toy_store.notification.application.dto.request.NotificationPreferenceRequest;
import com.example.new_toy_store.notification.application.dto.response.BroadcastNotificationResponse;
import com.example.new_toy_store.notification.application.dto.response.NotificationPreferenceResponse;
import com.example.new_toy_store.notification.application.dto.response.NotificationResponse;
import com.example.new_toy_store.notification.application.dto.response.UnreadNotificationCountResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationFacade facade;

    public NotificationController(NotificationFacade facade) {
        this.facade = facade;
    }

    @GetMapping
    public Page<NotificationResponse> getMyNotifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @ModelAttribute NotificationFilterRequest request,
            Pageable pageable
    ) {
        return facade.filterCurrentUser(userDetails.getUsername(), request, pageable);
    }

    @GetMapping("/unread-count")
    public UnreadNotificationCountResponse countUnread(@AuthenticationPrincipal UserDetails userDetails) {
        return facade.countCurrentUserUnread(userDetails.getUsername());
    }

    @GetMapping("/{id}")
    public NotificationResponse getDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id
    ) {
        return facade.getCurrentUserDetail(userDetails.getUsername(), id);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markAsRead(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id
    ) {
        return facade.markCurrentUserAsRead(userDetails.getUsername(), id);
    }

    @PatchMapping("/{id}/archive")
    public NotificationResponse archive(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Integer id
    ) {
        return facade.archiveCurrentUser(userDetails.getUsername(), id);
    }

    @PatchMapping("/read-all")
    public Map<String, Integer> markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        return Map.of("updatedCount", facade.markAllCurrentUserAsRead(userDetails.getUsername()));
    }

    @GetMapping("/preferences")
    public NotificationPreferenceResponse getPreferences(@AuthenticationPrincipal UserDetails userDetails) {
        return facade.getCurrentUserPreferences(userDetails.getUsername());
    }

    @PutMapping("/preferences")
    public NotificationPreferenceResponse updatePreferences(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody NotificationPreferenceRequest request
    ) {
        return facade.updateCurrentUserPreferences(userDetails.getUsername(), request);
    }

    @PostMapping("/broadcast")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public BroadcastNotificationResponse broadcast(@Valid @RequestBody BroadcastNotificationRequest request) {
        return facade.broadcast(request);
    }
}
