package com.ok.kafka.cluster.consumer.config;

import org.apache.kafka.common.TopicPartition;
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
 * Configures multi-threaded consumption and Dead Letter Topic (DLT) publishing.
 * The number of threads should match the number of partitions: only one thread
 * can read from a single partition, and partition count cannot be decreased.
 */
@Configuration
@EnableKafka
public class KafkaConfiguration {

    private static final String DLT_TOPIC_SUFFIX = ".dlt";

    private final ProducerFactory<Object, Object> producerFactory;
    private final ConsumerFactory<Object, Object> consumerFactory;

    public KafkaConfiguration(ProducerFactory<Object, Object> producerFactory,
                              ConsumerFactory<Object, Object> consumerFactory) {
        this.producerFactory = producerFactory;
        this.consumerFactory = consumerFactory;
    }

    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        // Route failed messages to the DLT.
        factory.setCommonErrorHandler(errorHandler);
        // Process messages using 4 threads (one per partition).
        factory.setConcurrency(4);
        return factory;
    }

    /**
     * Publisher that routes failed records to the dead-letter topic, keeping the
     * original partition. The DLT must have at least as many partitions as the
     * source topic, otherwise failed records would be lost.
     */
    @Bean
    public DeadLetterPublishingRecoverer publisher(KafkaTemplate<Object, Object> bytesTemplate) {
        return new DeadLetterPublishingRecoverer(bytesTemplate, (record, exception) ->
                new TopicPartition(record.topic() + DLT_TOPIC_SUFFIX, record.partition()));
    }

    /**
     * Default error handler that forwards any exception to the DLT without retries.
     */
    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer recoverer) {
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer);
        handler.addNotRetryableExceptions(Exception.class);
        return handler;
    }
}

