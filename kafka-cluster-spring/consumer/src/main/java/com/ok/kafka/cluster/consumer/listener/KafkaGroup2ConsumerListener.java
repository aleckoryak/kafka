package com.ok.kafka.cluster.consumer.listener;

import com.ok.kafka.cluster.consumer.dto.JsonMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumer group 2 — active only when {@code kafka.consumer.group2-enabled=true}
 * (the default). Set it to {@code false} for Scenario 1, which uses a single group.
 *
 * <p>Thread count is controlled by {@code kafka.consumer.group2-concurrency}:
 * <ul>
 *   <li>Scenario 2: 3 threads → one reads 2 partitions, two read 1 each</li>
 *   <li>Scenario 3: 4 threads → one thread per partition</li>
 * </ul>
 *
 * <p>Because this group is independent of group-1, every message published to
 * the topic is consumed twice in total (once per group).
 */
@Component
@ConditionalOnProperty(name = "kafka.consumer.group2-enabled", havingValue = "true", matchIfMissing = true)
public class KafkaGroup2ConsumerListener {

    @KafkaListener(
            id = "consumer-group-2",
            topics = "${kafka.topics.test-topic}",
            containerFactory = "kafkaListenerContainerFactory2")
    public void handle(@Payload JsonMessage message) {
        KafkaConsumerListeners.readMessage("group-2", message);
    }
}

