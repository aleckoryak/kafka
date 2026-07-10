# File System & Page Cache

**File System & Page Cache (файловая система и кэш страниц)** is the storage foundation that makes Kafka fast and durable. Kafka writes messages as a sequential log to the file system and relies on the OS page cache instead of an in-JVM cache.

- **Sequential I/O:** linear writes (~600 MB/s) vastly outperform random writes (~100 KB/s); the OS optimizes them with read-ahead and write-behind.
- **Page cache over heap:** avoids Java object overhead and GC pauses; stays warm across restarts.
- **Constant time (O(1)):** appending to a log decouples performance from data size, unlike BTree-based queues (O(log N)).
- **Long retention (удержание):** effectively unlimited cheap disk lets Kafka keep messages long after consumption.

### Best Practices
- Leave ample free RAM for the OS page cache; keep JVM heaps modest.
- Use large, sequential-friendly disks (JBOD) rather than expensive random-access storage.

### Production-Ready Recommendations
- Size RAM so the recent working set fits in cache, keeping consumers reading from memory.
- Avoid co-locating memory-hungry processes that evict the page cache.
- Plan storage capacity around retention (`retention.ms` / `retention.bytes`).

*References:*
- [Apache Kafka and the File System](../summaries/006_kafka_file_system.md)
- [Batching](Batching.md)

