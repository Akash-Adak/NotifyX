package com.notification_system.controller;


import com.notification_system.model.NotificationEntity;
import com.notification_system.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    // ✅ Get notifications
    @GetMapping("/{userId}")
    public ResponseEntity<Page<NotificationEntity>> getNotifications(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                service.getUserNotifications(userId, page, size)
        );
    }

    // ✅ Mark as read
    @PatchMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable UUID id) {
        service.markAsRead(id);
        return ResponseEntity.ok("Marked as read ✅");
    }

    // ✅ Unread count
    @GetMapping("/{userId}/unread-count")
    public ResponseEntity<Long> getUnread(@PathVariable String userId) {
        return ResponseEntity.ok(
                service.getUnreadCount(userId)
        );
    }
}