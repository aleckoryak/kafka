package com.ok.kafka.cluster.producer.service;

import com.ok.kafka.cluster.producer.dto.JsonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Publishes a batch of messages to the test topic.
 * <p>
 * When {@code kafka.producer.use-keys=true} (Scenario 1 & 3) each message
 * is sent with a random integer key; Kafka routes it to a partition by key hash.
 * <p>
 * When {@code kafka.producer.use-keys=false} (Scenario 2) no key is set and
 * Kafka's Sticky Partitioner fills one batch per partition in round-robin order.
 */
@Service
public class MessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(MessagePublisher.class);

    private final KafkaTemplate<String, JsonMessage> kafkaTemplate;

    @Value("${kafka.topics.test-topic}")
    private String testTopic;

    /** true → keyed (Scenario 1 & 3), false → keyless/Sticky (Scenario 2). */
    @Value("${kafka.producer.use-keys:true}")
    private boolean useKeys;

    public MessagePublisher(KafkaTemplate<String, JsonMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public long publish(long count) {
        log.info("Publishing {} messages to topic '{}' (use-keys={})", count, testTopic, useKeys);
        for (long i = 1; i <= count; i++) {
            JsonMessage message = new JsonMessage(i, "message-" + i);
            if (useKeys) {
                String key = Integer.toString(ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE));
                kafkaTemplate.send(testTopic, key, message);
            } else {
                // null key → Sticky Partitioner batches messages per partition
                kafkaTemplate.send(testTopic, message);
            }
        }
        kafkaTemplate.flush();
        log.info("Finished publishing {} messages", count);
        return count;
    }
}
