package com.resiflow.controller;

import com.resiflow.dto.NotificationResponse;
import com.resiflow.dto.UnreadNotificationCountResponse;
import com.resiflow.entity.NotificationType;
import com.resiflow.security.AuthenticatedUser;
import com.resiflow.service.NotificationService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "false") final boolean unreadOnly,
            @RequestParam(required = false) final List<NotificationType> types,
            @RequestParam(required = false) final Integer limit,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.getNotifications(authenticatedUser, unreadOnly, types, limit));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UnreadNotificationCountResponse> getUnreadCount(
            @RequestParam(required = false) final List<NotificationType> types,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(new UnreadNotificationCountResponse(
                notificationService.countUnreadNotifications(authenticatedUser, types)
        ));
    }

    @PutMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable final Long notificationId,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        return ResponseEntity.ok(notificationService.markAsRead(notificationId, authenticatedUser));
    }

    @PutMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(
            @RequestParam(required = false) final List<NotificationType> types,
            final Authentication authentication
    ) {
        AuthenticatedUser authenticatedUser = (AuthenticatedUser) authentication.getPrincipal();
        notificationService.markAllAsRead(authenticatedUser, types);
        return ResponseEntity.noContent().build();
    }
}
