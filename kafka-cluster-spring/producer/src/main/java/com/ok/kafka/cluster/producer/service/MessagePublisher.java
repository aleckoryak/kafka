package com.ok.kafka.cluster.producer.service;

import com.ok.kafka.cluster.producer.dto.JsonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Publishes a batch of messages to the test topic. Each message is sent with a
 * random integer key, so Kafka spreads the records across partitions by key hash.
 */
@Service
public class MessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(MessagePublisher.class);

    private final KafkaTemplate<String, JsonMessage> kafkaTemplate;

    @Value("${kafka.topics.test-topic}")
    private String testTopic;

    public MessagePublisher(KafkaTemplate<String, JsonMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public long publish(long count) {
        log.info("Publishing {} messages to topic '{}'", count, testTopic);
        for (long i = 1; i <= count; i++) {
            String key = Integer.toString(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
            JsonMessage message = new JsonMessage(i, "message-" + i);
            kafkaTemplate.send(testTopic, key, message);
        }
        kafkaTemplate.flush();
        log.info("Finished publishing {} messages", count);
        return count;
    }
}

