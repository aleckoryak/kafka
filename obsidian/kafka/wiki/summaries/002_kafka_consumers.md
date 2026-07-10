# Summary: What is a Kafka Consumer and How does it work?

**Source:** `raw/002. What is a Kafka Consumer and How does it work.md`
**Date Ingested:** 2026-07-09

## Key Takeaways
- **Kafka Consumers** subscribe to [Topics](../concepts/Topics.md) and poll for messages. They deserialize the message value and key.
- **Consumer Offsets:** A unique identifier for a message in a [Partition](../concepts/Partitions.md). Consumers "commit offsets" by saving which messages have been processed in a special topic to pick up where they left off. Java consumer does this automatically (at least once delivery).
- **Consumer Groups:** Consumers belong to a group. This adds parallelization. The number of active consumers in a group is limited by the number of partitions in the topic.
- **Partition Assignment:** Kafka automatically assigns partitions to consumers within a group. It handles "rebalancing" when consumers fail or are added.
- **Configuration:** Key configurations include bootstrap server connection, `group.id`, offset behavior (`earliest`, `latest`, `none`), and polling frequency.

## Concepts Covered
- [Consumers](../concepts/Consumers.md)
- [Consumer Groups](../concepts/Consumers.md#consumer-groups) (added to Consumers page)
- [Partitions](../concepts/Partitions.md)
