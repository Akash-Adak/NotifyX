package com.notification_system.stream;

import com.notification_system.config.JsonSerde;
import com.notification_system.model.NotificationEvent;

import com.notification_system.model.NotificationResponse;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class NotificationStreamProcessor {

    @Bean
    public KStream<String, NotificationEvent> process(StreamsBuilder builder) {

        KStream<String, NotificationEvent> stream =
                builder.stream("notifications");

        stream
                .groupBy((key, event) -> event.getUserId())
                .count(Materialized.as("like-count-store"))
                .toStream()
                .mapValues(count ->
                        new NotificationResponse(count + " people liked your post")
                )
                .to(
                        "aggregated-notifications",
                        Produced.with(Serdes.String(), new JsonSerde<>(NotificationResponse.class))
                );

        return stream;
    }

}