package com.notification_system.service;

import com.notification_system.model.NotificationEntity;
import com.notification_system.model.NotificationEvent;
import com.notification_system.repository.NotificationRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    // ✅ Save notification
    public void save(NotificationEvent event) {
        NotificationEntity entity =new  NotificationEntity();
                entity.setUserId(event.getUserId());
                entity.setType(event.getType());
                entity.setMessage(event.getMessage());
                entity.setCount(event.getCount());

        repository.save(entity);
    }

    // ✅ Get notifications
    public Page<NotificationEntity> getUserNotifications(String userId, int page, int size) {
        return repository.findByUserId(
                userId,
                PageRequest.of(page, size, Sort.by("createdAt").descending())
        );
    }

    // ✅ Mark as read
    public void markAsRead(UUID id) {
        NotificationEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        entity.setRead(true);
        repository.save(entity);
    }

    // ✅ Unread count
    public long getUnreadCount(String userId) {
        return repository.countByUserIdAndIsReadFalse(userId);
    }
}