package com.ok.kafka.cluster.producer.dto;

/**
 * Simple payload sent to Kafka: a sequential number and a text message.
 * Implemented as a Java record — immutable, no boilerplate.
 */
public record JsonMessage(long number, String message) {
}
