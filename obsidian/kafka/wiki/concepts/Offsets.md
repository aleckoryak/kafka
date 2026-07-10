# Offsets

An **Offset (офсет / смещение)** is a sequential integer identifier for a record within a [Partition](Partitions.md). It acts as a bookmark marking the next record a [Consumer](Consumers.md) should read.

- **Committed offset (закоммиченный офсет):** the next offset a consumer intends to read; checkpointed to the internal `__consumer_offsets` topic so consumers resume after failures/restarts.
- **High watermark (высокая отметка):** the last offset replicated to all replicas — consumers can only read up to it, preventing reads of unreplicated data.
- **Log end offset (LEO):** the offset of the last message written to the log.
- Small integer state makes consumer position cheap to track.

### Best Practices
- Treat the committed offset as "next to read," not "last processed."
- Disable auto-commit in production (`enable.auto.commit=false`) and commit manually.

### Production-Ready Recommendations
- Commit with `commitSync()` at the end of each `poll()` batch to bound reprocessing.
- Monitor **consumer lag** (LEO − committed offset) as a core health metric.
- For external sinks, persist offsets atomically with data (e.g. Transactional Outbox) to avoid duplicates.

*References:*
- [Kafka Consumer Design](../summaries/009_kafka_consumer_design.md)
- [Message Delivery Guarantees](../summaries/010_kafka_delivery_guarantees.md)
- [Consumer Groups](Consumer_Groups.md)

