# Kafka Streams

**Kafka Streams (Kafka Стримы)** is a client library for building real-time processing applications and microservices over data in Kafka, combining plain Java/Scala app simplicity with Kafka's server-side clustering.

- A Streams app is a **processing topology (топология обработки)**: source node (read topic) → processor node(s) (business logic: `mapValues`, `filter`, `groupByKey`, aggregations, joins) → sink node (write topic).
- Works with [Streams and Tables](Streams_and_Tables.md) (`KStream`/`KTable`) and stores state in fault-tolerant, compacted **changelog** topics — see [Log Compaction](Log_Compaction.md).
- Supports **exactly-once processing** via `processing.guarantee=exactly_once(_v2)` — see [Transactions](Transactions.md).

### Best Practices
- Define explicit Serdes and typed models; keep topologies focused (read → transform → write).
- Use KSQL/ksqlDB for SQL-style stream processing when code isn't needed.

### Case Studies
- **МегаФон:** filter balance-change events (`balance <= 0`) from `src` to `out` in real time.
- **WordCount / fraud detection:** stateful aggregation of a stream into a continuously-updated table.

### Production-Ready Recommendations
- Lightweight and embeddable, no external dependency beyond Kafka; fault-tolerant local state restored from changelog topics on failover.
- Per-record millisecond-latency processing; tune the commit interval to trade latency vs. overhead.

*References:*
- [Spring Boot + Kafka Streams](../summaries/022_spring_boot_kafka_streams.md)
- [Streams and Tables](../summaries/021_streams_and_tables.md)
- [Streams and Tables](Streams_and_Tables.md)
- [Transactions](Transactions.md)

