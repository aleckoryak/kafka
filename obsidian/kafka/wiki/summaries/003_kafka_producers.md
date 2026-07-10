# Summary: What are Kafka Producers and How do they work?

**Source:** `raw/003. What are Kafka Producers and How do they work.md`
**Date Ingested:** 2026-07-09

## Key Takeaways
- **Kafka Producers** are responsible for publishing messages to Kafka [Topics](../concepts/Topics.md).
- **Partition Assignment:** 
  - If a message has a **key**, the producer hashes the key and applies a modulo operation with the number of [Partitions](../concepts/Partitions.md) to route all identical keys to the same partition (ensuring order).
  - If the key is **null**, the producer uses a "sticky partition", batching messages and sending them to a partition to drive down latency while balancing load.
- **Producer Configuration:** Involves setting cluster connection details, creating a `KafkaProducer`, creating a `ProducerRecord` (with topic, key, value), and invoking the `send` method.
- **Producer Acks (Acknowledgments):** Determines delivery guarantees:
  - `acks=0` (None): Lowest latency, highest risk of data loss. Producer doesn't wait for acknowledgment.
  - `acks=1`: Producer waits for the partition's leader broker to acknowledge receipt.
  - `acks=-1` (or `all`): Producer waits for the leader and all in-sync replicas (followers) to acknowledge. Slowest but safest.

## Concepts Covered
- [Producers](../concepts/Producers.md)
- [Partitions](../concepts/Partitions.md)
- [Replication](../concepts/Replication.md)
