package com.ok.kafka.cluster.consumer.listener;

import com.ok.kafka.cluster.consumer.dto.JsonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Two listeners placed in different consumer groups (via the {@code id}
 * attribute), so every message is processed twice. Messages whose number is a
 * multiple of 100 throw an exception and are routed to the DLT.
 */
@Component
public class KafkaConsumerListeners {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerListeners.class);

    @KafkaListener(
            id = "consumer-group-1",
            topics = "${kafka.topics.test-topic}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handle(@Payload JsonMessage message) {
        readMessage(message);
    }

    @KafkaListener(
            id = "consumer-group-2",
            topics = "${kafka.topics.test-topic}",
            containerFactory = "kafkaListenerContainerFactory")
    public void handle2(@Payload JsonMessage message) {
        readMessage(message);
    }

    private void readMessage(JsonMessage message) {
        long number = message.number();
        String currentThreadName = Thread.currentThread().getName();
        log.info("Read message with number: {} on thread: {}", number, currentThreadName);
        if (number % 100 == 0) {
            log.info("Message is a multiple of 100, routing to DLT");
            throw new RuntimeException("Received message with number that is a multiple of 100");
        }
    }
}

