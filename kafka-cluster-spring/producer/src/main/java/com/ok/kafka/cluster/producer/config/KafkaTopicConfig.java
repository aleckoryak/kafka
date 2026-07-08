package com.ok.kafka.cluster.producer.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Declares the main topic and its Dead Letter Topic (DLT).
 * Both topics must have the same number of partitions, because the DLT
 * publisher preserves the original partition when routing failed messages.
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topics.test-topic}")
    private String testTopic;

    @Value("${kafka.topics.partitions}")
    private int partitions;

    @Value("${kafka.topics.replication-factor}")
    private short replicationFactor;

    @Bean
    public NewTopic testTopic() {
        return TopicBuilder.name(testTopic)
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }

    @Bean
    public NewTopic testTopicDlt() {
        return TopicBuilder.name(testTopic + ".dlt")
                .partitions(partitions)
                .replicas(replicationFactor)
                .build();
    }
}

