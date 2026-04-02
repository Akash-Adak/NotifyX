package com.notification_system.stream;

import com.notification_system.config.JsonSerde;
import com.notification_system.model.NotificationEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.unbounded;

@Configuration
public class NotificationStreamProcessor {

    @Bean
    public KStream<String, NotificationEvent> process(StreamsBuilder builder) {

        System.out.println("🚀 STREAM STARTED"); // 👈 ADD THIS
        KStream<String, NotificationEvent> stream =
                builder.stream(
                        "notifications",
                        Consumed.with(Serdes.String(), new JsonSerde<>(NotificationEvent.class))
                );

        stream.peek((k, v) -> System.out.println("🔥 INPUT: " + v));

        stream
                .groupBy((key, event) -> event.getUserId())

                .windowedBy(TimeWindows.ofSizeAndGrace(
                        Duration.ofSeconds(5),
                        Duration.ofSeconds(2)
                ))

                .count(Materialized.as("like-count-window-store"))

                // ✅ CRITICAL FIX
//                .suppress(Suppressed.untilWindowCloses(unbounded()))

                .toStream()

                .map((windowedKey, count) -> {
                    String userId = windowedKey.key();

                    NotificationEvent event = new NotificationEvent();
                    event.setUserId(userId);
                    event.setMessage(count + " likes in last 5 seconds");
                    event.setType("LIKE_ALERT"); // ✅ add this
                    event.setTimestamp(System.currentTimeMillis());
                    return new KeyValue<>(userId, event);
                })

                .peek((k, v) -> System.out.println("🔥 OUTPUT: " + v.getMessage()))

                .to(
                        "aggregated-notifications",
                        Produced.with(Serdes.String(), new JsonSerde<>(NotificationEvent.class))
                );

        return stream;
    }

    public NotificationStreamProcessor() {
        System.out.println("✅ STREAM CONFIG LOADED");
    }
}