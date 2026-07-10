# Log Compaction

**Log Compaction (сжатие лога)** is a key-based retention policy (`cleanup.policy=compact`) that keeps the latest value for each message **key (ключ)** and discards older values, guaranteeing a full snapshot of the final state for every key. Contrast with time/size-based [Topic](Topics.md) retention.

- **Tombstones (маркеры удаления):** a key with a `null` payload deletes that key; tombstones are removed after `delete.retention.ms` (default 24h).
- The log has a dense **head** (all messages) and a compacted **tail**; **offsets never change** and remain valid even if compacted away (gaps are skipped by consumers).
- Cleaning runs in the background (offset map → scan → retain latest → swap segments), does not block reads, and is I/O-throttleable.
- Guarantee: you always see the *latest* value per key, but **not necessarily every update**.

### Best Practices
- Requires keyed records with consistent, meaningful keys.
- Protect fresh data with `min.compaction.lag.ms`; tune `min.cleanable.dirty.ratio` (CPU vs. disk).

### Case Studies
- **CDC (Debezium)** keyed by primary key; **cache warm-up** on microservice restart; **Kafka Streams changelogs** for KTable state.

### Production-Ready Recommendations
- Set `delete.retention.ms` ≥ max consumer downtime so consumers don't miss tombstones.
- Scale `log.cleaner.threads`; use SSDs; alert on `uncleanable.partitions.count`, `max.clean.time.secs`, `max.compaction.delay.secs`.

*References:*
- [Kafka Log Compaction](../summaries/014_kafka_log_compaction.md)
- [Topic Compaction (deep dive)](../summaries/015_kafka_topic_compaction.md)
- [Kafka Streams](Kafka_Streams.md)
- [Topics](Topics.md)

