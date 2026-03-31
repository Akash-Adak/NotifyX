package com.notification_system.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class NotificationConsumer {

    @KafkaListener(topics = "processed-notifications", groupId = "notification-group")
    public void consume(String message) {
        System.out.println("FINAL NOTIFICATION 👉 " + message);
    }
}