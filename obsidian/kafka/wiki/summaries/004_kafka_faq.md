# Summary: Kafka Frequently Asked Questions

**Source:** `raw/004. Kafka Frequently Asked Questions.md`
**Date Ingested:** 2026-07-09

## Key Takeaways
- **Benefits of Kafka:** High throughput, horizontal scalability, durability (long-term storage), fault tolerance (via replication), low latency, decoupling of systems, and replayability of historical data.
- **Compared to Traditional Messaging:** Kafka is log-based (append-only) rather than queue-based. It does not delete messages after consumption, allowing replayability and multi-subscriber consumption without data loss. It is designed for massive throughput and parallel processing (via partitioning).
- **Use Cases:** Real-time data pipelines, event-driven architectures, stream processing, activity tracking, messaging, data integration, and log aggregation.

## Concepts Covered
- Re-enforces core concepts: [Brokers](../concepts/Brokers.md), [Topics](../concepts/Topics.md), [Partitions](../concepts/Partitions.md), [Producers](../concepts/Producers.md), [Consumers](../concepts/Consumers.md), [Replication](../concepts/Replication.md).
