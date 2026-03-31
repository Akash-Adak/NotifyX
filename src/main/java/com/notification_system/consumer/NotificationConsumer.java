package com.notification_system.consumer;



import com.notification_system.model.NotificationEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void consume(NotificationEvent event) {

        System.out.println("FINAL 👉 " + event);

        // 🚀 SEND TO FRONTEND
        messagingTemplate.convertAndSend("/topic/notifications", event);
    }
}