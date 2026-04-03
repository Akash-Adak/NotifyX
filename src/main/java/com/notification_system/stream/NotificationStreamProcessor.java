package com.notification_system.stream;

import com.notification_system.config.JsonSerde;
import com.notification_system.model.NotificationEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class NotificationStreamProcessor {

    @Bean
    public KStream<String, NotificationEvent> process(StreamsBuilder builder) {

        System.out.println("🚀 STREAM STARTED");

        KStream<String, NotificationEvent> stream =
                builder.stream(
                        "notifications",
                        Consumed.with(Serdes.String(), new JsonSerde<>(NotificationEvent.class))
                );

        // 🔥 DEBUG INPUT
        stream.peek((k, v) -> System.out.println("🔥 INPUT: " + v));

        stream

                // ✅ STEP 1: FILTER INVALID EVENTS
                .filter((key, event) ->
                        event != null &&
                                event.getUserId() != null
//                                event.getType() != null
                )

                // ✅ STEP 2: GROUP BY USER
                .groupBy((key, event) -> event.getUserId())

                // ✅ STEP 3: WINDOW (5 sec)
                .windowedBy(TimeWindows.ofSizeAndGrace(
                        Duration.ofSeconds(5),
                        Duration.ofSeconds(2)
                ))
                .emitStrategy(EmitStrategy.onWindowUpdate())
                // ✅ STEP 4: AGGREGATE (FIXED LOGIC)
                .aggregate(

                        // initializer
                        () -> {
                            NotificationEvent e = new NotificationEvent();
                            e.setCount(0);
                            return e;
                        },

                        // aggregator
                        (userId, newEvent, aggEvent) -> {

                            aggEvent.setUserId(newEvent.getUserId());
                            aggEvent.setType(newEvent.getType());

                            // 🔥 ALWAYS increment
                            aggEvent.setCount(aggEvent.getCount() + 1);

                            aggEvent.setTimestamp(System.currentTimeMillis());

                            return aggEvent;
                        },

                        Materialized.with(
                                Serdes.String(),
                                new JsonSerde<>(NotificationEvent.class)
                        )
                )

                // ✅ STEP 5: CONVERT TO NORMAL STREAM
                .toStream()

                // ✅ STEP 6: MAP TO OUTPUT EVENT
                .map((windowedKey, event) -> {

                    String userId = windowedKey.key();

                    NotificationEvent output = new NotificationEvent();
                    output.setUserId(userId);
                    output.setType(event.getType());

                    int count = event.getCount();
                    output.setCount(count);
                    output.setMessage(
                            count + " " +
                                    event.getType() +
                                    " in last 5 seconds"
                    );

                    output.setTimestamp(System.currentTimeMillis());

                    return new KeyValue<>(userId, output);
                })

                // ✅ STEP 7: FILTER (THRESHOLD)
                .filter((key, event) -> event.getCount() >=1)

                // 🔥 DEBUG OUTPUT
                .peek((k, v) -> System.out.println("🔥 OUTPUT: " + v.getMessage()))

                // ✅ STEP 8: SEND TO OUTPUT TOPIC
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