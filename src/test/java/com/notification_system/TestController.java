package com.notification_system;

import com.notification_system.model.NotificationEvent;
import com.notification_system.producer.KafkaProducerService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    private final KafkaProducerService producer;

    public TestController(KafkaProducerService producer) {
        this.producer = producer;
    }

    @PostMapping("/like/{userId}")
    public String send(@PathVariable String userId) {

        NotificationEvent event = new NotificationEvent();
        event.setUserId(userId);

        producer.sendEvent(event);

        return "Sent!";
    }
}