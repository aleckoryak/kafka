# Summary: Introduction to Apache Kafka

**Source:** `raw/001. What is Kafka Topics, Producers, Consumers, Brokers Explained.md`
**Date Ingested:** 2026-07-09

## Key Takeaways
- **Apache Kafka** is a distributed event streaming platform used for real-time data pipelines.
- An **event** is a record that something happened (key, value, timestamp).
- **Core Architecture:** 
  - [Producers](../concepts/Producers.md) write events to [Topics](../concepts/Topics.md).
  - [Consumers](../concepts/Consumers.md) read events from Topics.
  - [Brokers](../concepts/Brokers.md) form the cluster that stores and serves the data.
- **Topics** are partitioned and replicated for scalability and fault tolerance.
- **Additional Components:**
  - **Kafka Connect:** Moves data in and out of Kafka via source/sink connectors.
  - **Kafka Streams:** A library for complex event processing and transformations.

## Concepts Covered
- [Brokers](../concepts/Brokers.md)
- [Topics](../concepts/Topics.md)
- [Producers](../concepts/Producers.md)
- [Consumers](../concepts/Consumers.md)
- [Partitions](../concepts/Partitions.md)
- [Replication](../concepts/Replication.md)
