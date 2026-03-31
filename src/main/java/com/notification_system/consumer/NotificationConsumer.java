package com.notification_system.consumer;



import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "processed-notifications", groupId = "notification-group")
    public void consume(String message) {

        System.out.println("FINAL 👉 " + message);

        // 🚀 SEND TO FRONTEND
        messagingTemplate.convertAndSend("/topic/notifications", message);
    }
}