package com.ok.kafka.cluster.consumer.listener;

import com.ok.kafka.cluster.consumer.dto.JsonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumer group 1 — always active.
 *
 * <p>Thread count is controlled by {@code kafka.consumer.group1-concurrency}:
 * <ul>
 *   <li>Scenario 1: 2 threads → each reads 2 of the 4 partitions</li>
 *   <li>Scenario 2: 2 threads → same split</li>
 *   <li>Scenario 3: 4 threads → one thread per partition</li>
 * </ul>
 */
@Component
public class KafkaConsumerListeners {

    static final Logger log = LoggerFactory.getLogger(KafkaConsumerListeners.class);

    @KafkaListener(
            id = "consumer-group-1",
            topics = "${kafka.topics.test-topic}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handle(@Payload JsonMessage message) {
        readMessage("group-1", message);
    }

    /**
     * Shared handler logic reused by both consumer groups.
     * Throws a {@link RuntimeException} for every 100th message so it is
     * routed to the DLT, demonstrating dead-letter behaviour.
     */
    static void readMessage(String group, JsonMessage message) {
        long number = message.number();
        String thread = Thread.currentThread().getName();
        log.info("[{}] Read message #{} on thread {}", group, number, thread);
        if (number % 100 == 0) {
            log.warn("[{}] Message #{} is a multiple of 100 — routing to DLT", group, number);
            throw new RuntimeException("Message number is a multiple of 100: " + number);
        }
    }
}
