package com.notification_system.consumer;

import com.notification_system.model.NotificationEvent;
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

//    @KafkaListener(
//            topics = "aggregated-notifications",
//            groupId = "notification-group-v2"
//    )
//    public void consume(NotificationEvent event) {
//
//        System.out.println("🔥 FINAL NOTIFICATION: " + event.getMessage());
//
//        messagingTemplate.convertAndSend("/topic/notifications", event);
//    }



    @RetryableTopic(
            attempts = "3",   // total attempts
            backoff = @Backoff(delay = 2000), // 2 sec delay
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltTopicSuffix = "-dlq"
    )
    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void consume(NotificationEvent event) {

        System.out.println("📩 Received: " + event);

        // simulate failure
        if (event.getType() == null) {
            System.out.println("❌ Error: Invalid event");
            throw new RuntimeException("Invalid event");
        }

        System.out.println("✅ Processed successfully");
    }
}