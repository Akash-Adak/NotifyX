package com.notification_system.consumer;

import com.notification_system.model.NotificationDLQEntity;
import com.notification_system.model.NotificationEntity;
import com.notification_system.model.NotificationEvent;
import com.notification_system.repository.NotificationDLQRepository;
import com.notification_system.repository.NotificationRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    @Autowired
    private NotificationRepository repository;
    @Autowired
    private NotificationDLQRepository dlqRepository;

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 2000, multiplier = 2),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltTopicSuffix = "-dlq"
    )
    @KafkaListener(
            topics = "aggregated-notifications",
            groupId = "notification-group"
    )
    public void consume(NotificationEvent event) {
        log.info("Received notification event: {}", event);

        if (event == null || event.getType() == null) {
            log.warn("Invalid notification event received. Sending to retry/DLQ. Event: {}", event);
            throw new RuntimeException("Invalid notification event: type is null");
        }
        // ✅ Save to DB
        NotificationEntity entity = mapToEntity(event);
        repository.save(entity);

        messagingTemplate.convertAndSend("/topic/notifications", event);
        log.info("Notification pushed to WebSocket topic for userId={}, type={}", event.getUserId(), event.getType());
    }

    private NotificationEntity mapToEntity(NotificationEvent event) {
        NotificationEntity entity = new NotificationEntity();
        entity.setUserId(event.getUserId());
        entity.setType(event.getType());
        entity.setMessage(event.getMessage());
        entity.setCreatedAt(System.currentTimeMillis());
        return entity;
    }
    private NotificationDLQEntity mapToDLQEntity(NotificationEvent event) {
        NotificationDLQEntity entity = new NotificationDLQEntity();
        entity.setUserId(event.getUserId());
        entity.setType(event.getType());
        entity.setMessage(event.getMessage());
        entity.setCreatedAt(System.currentTimeMillis());
        return entity;
    }
    @KafkaListener(
            topics = "aggregated-notifications-dlq",
            groupId = "dlq-group"
    )
    public void consumeDLQ(NotificationEvent event) {
        log.error("DLQ notification event received: {}", event);
        NotificationDLQEntity entity = mapToDLQEntity(event);
        dlqRepository.save(entity);
    }

    @DltHandler
    public void handleDLT(NotificationEvent event) {
        log.error("Final failed notification event reached DLT handler: {}", event);
    }
}
