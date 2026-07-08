package com.ok.kafka.cluster.consumer.dto;

/**
 * Payload received from Kafka. Must match the producer's structure so Jackson
 * can deserialize it from JSON via the canonical record constructor.
 */
public record JsonMessage(long number, String message) {
}
