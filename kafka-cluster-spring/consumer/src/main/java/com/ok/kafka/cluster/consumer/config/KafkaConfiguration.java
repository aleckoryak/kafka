package com.ok.kafka.cluster.consumer.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;

/**
 * Configures two independent listener container factories — one per consumer group —
 * so that each group can have a different concurrency (thread count).
 *
 * <pre>
 * Scenario 1: group1-concurrency=2, group2-enabled=false
 * Scenario 2: group1-concurrency=2, group2-concurrency=3, group2-enabled=true
 * Scenario 3: group1-concurrency=4, group2-concurrency=4, group2-enabled=true
 * </pre>
 *
 * Thread count = number of partitions this group's consumers will read in parallel.
 * A single thread can read from multiple partitions, but one partition is only ever
 * read by one thread within the same group.
 */
@Configuration
@EnableKafka
public class KafkaConfiguration {

    private static final String DLT_TOPIC_SUFFIX = ".dlt";

    private final ProducerFactory<Object, Object> producerFactory;
    private final ConsumerFactory<Object, Object> consumerFactory;

    /** Concurrency (thread count) for consumer-group-1. */
    @Value("${kafka.consumer.group1-concurrency:4}")
    private int group1Concurrency;

    /** Concurrency (thread count) for consumer-group-2. */
    @Value("${kafka.consumer.group2-concurrency:4}")
    private int group2Concurrency;

    public KafkaConfiguration(ProducerFactory<Object, Object> producerFactory,
                              ConsumerFactory<Object, Object> consumerFactory) {
        this.producerFactory = producerFactory;
        this.consumerFactory = consumerFactory;
    }

    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory);
    }

    /** Factory for consumer-group-1. */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            DefaultErrorHandler errorHandler) {
        return buildFactory(errorHandler, group1Concurrency);
    }

    /** Factory for consumer-group-2 (separate concurrency setting). */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory2(
            DefaultErrorHandler errorHandler) {
        return buildFactory(errorHandler, group2Concurrency);
    }

    private ConcurrentKafkaListenerContainerFactory<Object, Object> buildFactory(
            DefaultErrorHandler errorHandler, int concurrency) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        factory.setConcurrency(concurrency);
        return factory;
    }

    /**
     * Routes failed records to the dead-letter topic on the same partition.
     * The DLT must have at least as many partitions as the source topic.
     */
    @Bean
    public DeadLetterPublishingRecoverer publisher(KafkaTemplate<Object, Object> bytesTemplate) {
        return new DeadLetterPublishingRecoverer(bytesTemplate, (record, exception) ->
                new TopicPartition(record.topic() + DLT_TOPIC_SUFFIX, record.partition()));
    }

    /** Sends any exception immediately to the DLT — no retries. */
    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer);
        handler.addNotRetryableExceptions(Exception.class);
        return handler;
    }
}
