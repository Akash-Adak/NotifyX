package com.notification_system.repository;


import com.notification_system.model.NotificationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID> {

    Page<NotificationEntity> findByUserId(String userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(String userId);
}