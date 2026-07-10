# Streams and Tables

**Streams and Tables (стримы и таблицы)** are the two core abstractions of Kafka stream processing.

- **Stream (стрим/поток):** the full history of events (past + present), append-only — a `KStream` / KSQL `STREAM` (a [Topic](Topics.md) + schema).
- **Table (таблица):** the current state (present), an aggregation of events continuously updated by key (UPSERT) — a `KTable` / KSQL `TABLE`.
- **Aggregations (агрегации)** are computed per key and always return a table.
- **Stream–table duality (двойственность стрим-таблица):** a table has an internal **changelog stream (стрим изменений)** (like DB CDC); streams ⇄ tables. Changelogs are compacted topics enabling elasticity, fault tolerance, and state migration between machines.
- Databases think table-first; Kafka thinks stream-first ("database inside-out") — making it **stream-relational**.

### Best Practices
- Use tables for stateful joins/enrichment and aggregations; avoid external stores when Streams/KSQL suffice.
- Use Avro + Schema Registry to balance schema-on-read vs. schema-on-write.

### Case Studies
- **Geolocation:** aggregate `<User, GeoLocation>` into latest-location or visited-count tables.
- **New York Times:** 160+ years of articles stored in Kafka as source of truth.

### Production-Ready Recommendations
- KTable state is restored from changelog topics on failover — treat changelogs as critical backups.
- Remember aggregations are per-key *per-partition*; account for custom partitioners.

*References:*
- [Of Streams and Tables (summary)](../summaries/021_streams_and_tables.md)
- [Kafka Streams](Kafka_Streams.md)
- [Log Compaction](Log_Compaction.md)

