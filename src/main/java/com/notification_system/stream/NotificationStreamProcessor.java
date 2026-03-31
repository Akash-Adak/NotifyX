package com.notification_system.stream;

import com.notification_system.config.JsonSerde;
import com.notification_system.model.NotificationEvent;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class NotificationStreamProcessor {

    @Bean
    public KStream<String, NotificationEvent> process(StreamsBuilder builder) {

        KStream<String, NotificationEvent> stream =
                builder.stream("notifications",
                        Consumed.with(Serdes.String(), new JsonSerde<>(NotificationEvent.class)));

        stream
                // group by user + type (IMPORTANT IMPROVEMENT)
                .groupBy((key, value) -> key + "-" + value.getType(),
                        Grouped.with(Serdes.String(), new JsonSerde<>(NotificationEvent.class)))

                .windowedBy(TimeWindows.of(Duration.ofDays(10000)))

                .count()

                .toStream()

                .map((key, count) -> KeyValue.pair(
                        key.key(),
                        key.key() + " → You got " + count + " " + extractType(key.key()) + " notifications"
                ))

                .to("processed-notifications",
                        Produced.with(Serdes.String(), Serdes.String()));

        return stream;
    }

    private String extractType(String key) {
        return key.split("-")[1];
    }
}