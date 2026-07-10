# Dead Letter Queue

A **Dead Letter Queue / Topic (DLQ/DLT, очередь недоставленных сообщений)** is a separate topic that receives messages a consumer fails to process (deserialization errors or business-logic exceptions), preventing a **Poison Pill (ядовитое сообщение)** from blocking a partition.

- After a configured number of failed attempts, the message is published to the DLT for manual/automated inspection, and the consumer moves on.
- In Spring Kafka: `DeadLetterPublishingRecoverer` + `DefaultErrorHandler`.

### Best Practices
- The DLT should have the **same number of partitions** as the source topic (the recoverer preserves the source partition), otherwise messages can be lost.
- Distinguish retryable vs. non-retryable exceptions; only dead-letter the latter (or after retries are exhausted).

### Case Studies
- Consumer throwing on `number % 100 == 0`: with 2 consumer groups over 10,000 messages, 200 land in `topic1.dlt`.
- Malformed JSON caught by `ErrorHandlingDeserializer` and routed to the DLT.

### Production-Ready Recommendations
- Monitor DLT depth and alert on growth; build a replay/inspection workflow.
- Preserve original headers/partition for traceability.

*References:*
- [Kafka Cluster & docker-compose](../summaries/023_kafka_cluster_docker_compose.md)
- [Message Delivery Semantics](../summaries/017_message_delivery_semantics.md)
- [Delivery Semantics](Delivery_Semantics.md)

