# Consumer Groups

A **Consumer Group (группа потребителей)** is a set of [Consumers](Consumers.md) sharing a `group.id` that cooperatively consume one or more [Topics](Topics.md). Each [Partition](Partitions.md) is consumed by exactly one member of a group at a time, enabling parallel, scalable processing.

- **Group coordinator (координатор группы):** a broker-side component (selected by `group.id`) that assigns partitions and rebalances on membership/metadata changes, tracking state in `__consumer_offsets`.
- **Rebalance protocols (протоколы ребалансировки):**
  - **Classic:** a client-side group leader computes assignments; eager/cooperative; high disruption (all consumers may pause).
  - **Consumer (new):** server-side coordinator computes incremental assignments; low disruption; GA in Kafka 4.0 via `group.protocol=consumer`.

### Best Practices
- Match the number of consumers to the number of partitions — surplus consumers stay idle.
- Adopt the new consumer rebalance protocol (Kafka 4.0+) to avoid stop-the-world rebalances.

### Production-Ready Recommendations
- Design consumers to be idempotent, since reassignment resumes from the last committed offset (reprocessing possible).
- Monitor rebalance frequency and consumer lag per group.

*References:*
- [Kafka Consumer Design](../summaries/009_kafka_consumer_design.md)
- [Consumers](Consumers.md)
- [Offsets](Offsets.md)

