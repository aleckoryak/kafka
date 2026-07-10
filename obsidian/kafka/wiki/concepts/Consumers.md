# Consumers

**Consumers (консьюмеры / получатели)** are clients that read events from Kafka [Topics](Topics.md).
- They maintain their own state via an [offset](Offsets.md), allowing them to read records sequentially, reprocess older data, or skip ahead.
- **Pull model (модель запроса):** consumers pull data from brokers, providing flow control and optimal batching without added latency.
- Their independent offset management means consumers can join or leave without disrupting the cluster.
- **Committing Offsets:** Consumers save their progress (offsets) in a special Kafka topic (`__consumer_offsets`) so they can resume after a failure. This can be handled automatically (e.g., in the Java client).

## Consumer Groups
Each consumer belongs to a **Consumer Group (группа потребителей)**, which allows for parallel processing of a topic's messages. See [Consumer Groups](Consumer_Groups.md).
- The maximum number of active consumers in a group is equal to the number of [Partitions](Partitions.md) in the topic.
- **Partition Assignment:** A broker-side **Group coordinator (координатор группы)** assigns partitions to consumers and handles **rebalancing (ребалансировка)** if a consumer fails or a new one is added.
- Kafka 4.0 adds a low-disruption server-side "consumer" rebalance protocol alongside the "classic" one.

### Best Practices
- Match consumer count to partition count — surplus consumers stay idle.
- Disable auto-commit in production (`enable.auto.commit=false`) and commit manually after each `poll()` batch.

### Production-Ready Recommendations
- Design consumers to be idempotent; reassignment resumes from the last committed offset.
- Never rely on reading past the high watermark — see [Offsets](Offsets.md).
- Monitor consumer lag as a core health metric — see [Delivery Semantics](Delivery_Semantics.md).

*References:*
- [Introduction to Apache Kafka](../summaries/001_introduction.md)
- [What is a Kafka Consumer and How does it work?](../summaries/002_kafka_consumers.md)
- [Kafka Consumer Design](../summaries/009_kafka_consumer_design.md)
