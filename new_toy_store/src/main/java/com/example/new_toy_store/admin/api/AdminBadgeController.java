package com.example.new_toy_store.admin.api;

import com.example.new_toy_store.admin.application.dto.response.AdminMenuBadgeResponse;
import com.example.new_toy_store.admin.application.AdminBadgeQuery;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/menu-badges")
public class AdminBadgeController {

    private final AdminBadgeQuery badgeQuery;

    public AdminBadgeController(AdminBadgeQuery badgeQuery) {
        this.badgeQuery = badgeQuery;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'MANAGER')")
    public AdminMenuBadgeResponse getMenuBadges() {
        return badgeQuery.getMenuBadges();
    }
}
