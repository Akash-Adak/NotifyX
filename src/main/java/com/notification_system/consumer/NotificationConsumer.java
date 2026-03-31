package com.notification_system.consumer;



import com.notification_system.model.NotificationEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationConsumer {
    private Map<String, Integer> likeCounter = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }


    @KafkaListener(topics = "notifications", groupId = "notification-group")
    public void consume(NotificationEvent event) {

        String key = event.getUserId();

        int count = likeCounter.getOrDefault(key, 0) + 1;
        likeCounter.put(key, count);

        System.out.println("🔥 Aggregated count: " + count);

        if (count == 1 || count % 5 == 0) {

            Map<String, Object> data = new HashMap<>();
            data.put("message", count + " people liked your post");

            messagingTemplate.convertAndSend("/topic/notifications", data);
        }
    }


    @Scheduled(fixedRate = 60000) // every 1 min
    public void clearCounters() {
        likeCounter.clear();
    }
}