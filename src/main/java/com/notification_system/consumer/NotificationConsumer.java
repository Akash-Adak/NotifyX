package com.notification_system.consumer;

import com.notification_system.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

        messagingTemplate.convertAndSend("/topic/notifications", event);
        log.info("Notification pushed to WebSocket topic for userId={}, type={}", event.getUserId(), event.getType());
    }

    @KafkaListener(
            topics = "aggregated-notifications-dlq",
            groupId = "dlq-group"
    )
    public void consumeDLQ(NotificationEvent event) {
        log.error("DLQ notification event received: {}", event);
    }

    @DltHandler
    public void handleDLT(NotificationEvent event) {
        log.error("Final failed notification event reached DLT handler: {}", event);
    }
}