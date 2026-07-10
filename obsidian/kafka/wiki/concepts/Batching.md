# Batching

**Batching (батчинг / наборы сообщений)** groups multiple messages together to amortize network round-trips and turn bursty random writes into large linear writes, improving throughput by orders of magnitude.

- **Message sets:** the Kafka protocol groups records; brokers append chunks in one go and consumers fetch large linear chunks.
- **Zero-copy (нулевое копирование):** with `sendfile`, data is copied into the page cache once and streamed to the network.
- **End-to-end compression (сквозное сжатие):** batches are compressed by the producer, stored/transmitted compressed, and decompressed by the consumer. Codecs: GZIP, Snappy, LZ4, ZStandard.
- **Producer controls:** `batch.size` (bytes, default 16 KB) and `linger.ms` (default 0) — a batch ships when either is reached.

### Best Practices
- Enable compression (`lz4`/`zstd`) only alongside real batching (`linger.ms > 0`).
- Keep `max.request.size` and broker `message.max.bytes` consistent to avoid `RecordTooLargeException`.

### Production-Ready Recommendations
- Throughput profile: `linger.ms=5–20`, `batch.size=65536`+, scale `buffer.memory` (default 32 MB) to 64–128 MB with large batches or many partitions.
- Keep `enable.idempotence=true` and `max.in.flight.requests.per.connection` ≤ 5 to preserve ordering on retries.

*References:*
- [Kafka Batch Processing for Efficiency](../summaries/007_kafka_batching.md)
- [Kafka Producer Design](../summaries/008_kafka_producer_design.md)
- [File System & Page Cache](File_System_and_Page_Cache.md)

