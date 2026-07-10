# Partitions

**Partitions (партиции / разделы)** allow a single [Topic](Topics.md) to be broken into multiple logs across different brokers.
- This enables scalability by parallelizing reads and writes.
- Events with the same key are written to the same partition, guaranteeing strict ordering for that key.
- Each partition is consumed by exactly one member of a [Consumer Group](Consumer_Groups.md) at a time.
- Position within a partition is tracked by an [Offset](Offsets.md).

### Best Practices
- Set partition count to the desired maximum consumer parallelism.
- Use key-based partitioning (`hash(key) % N`) when per-key ordering is required.

### Production-Ready Recommendations
- Avoid excessive partitions per broker (overhead in metadata, rebalancing, open files).
- Plan partition count ahead — increasing it later breaks key-to-partition ordering guarantees.

*References:*
- [Introduction to Apache Kafka](../summaries/001_introduction.md)
- [Kafka Producer Design](../summaries/008_kafka_producer_design.md)
