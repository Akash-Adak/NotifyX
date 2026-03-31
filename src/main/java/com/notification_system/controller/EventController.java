package com.notification_system.controller;

import com.notification_system.model.NotificationEvent;
import com.notification_system.producer.KafkaProducerService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final KafkaProducerService producer;

    public EventController(KafkaProducerService producer) {
        this.producer = producer;
    }

    @PostMapping
    public String send(@RequestBody NotificationEvent event) {
        producer.sendEvent(event);
        return "Event sent 🚀";
    }
}