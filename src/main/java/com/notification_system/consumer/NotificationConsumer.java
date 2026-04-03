package com.notification_system.consumer;

import com.notification_system.model.NotificationEvent;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;
@Service
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // ✅ THIS CONSUMES FROM STREAM OUTPUT
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

        System.out.println("📩 Received: " + event);

        // ❌ simulate failure
        if (event.getType() == null) {
            System.out.println("❌ Error: Invalid event");
            throw new RuntimeException("Invalid event");
        }

        System.out.println("✅ Sending to WebSocket");

        // ✅ send to UI
        messagingTemplate.convertAndSend("/topic/notifications", event);
    }

    // 💀 DLQ LISTENER
    @KafkaListener(
            topics = "aggregated-notifications-dlq",
            groupId = "dlq-group"
    )
    public void consumeDLQ(NotificationEvent event) {
        System.out.println("💀 DLQ EVENT: " + event);

        // 👉 later: store in DB
    }


    @DltHandler
    public void handleDLT(NotificationEvent event) {
        System.out.println("🚨 FINAL FAILED (DLT HANDLER): " + event);
    }
}