# Topics

A **Topic (топик)** is an ordered log of events (records) stored durably in Kafka, acting as Kafka's fundamental unit of organization.
- They are similar to folders in a filesystem.
- **Append-only:** New messages are added to the end of the log.
- **Immutable:** Events cannot be modified once written.
- **Multi-producer & Multi-subscriber:** Multiple clients can write to and read from a topic.
- Data is retained based on time or size limits, not deleted upon consumption. This is enabled by cheap sequential storage — see [File System & Page Cache](File_System_and_Page_Cache.md).

### Best Practices
- Choose partition count based on target parallelism and consumer count.
- Set retention (`retention.ms` / `retention.bytes`) deliberately for storage planning.

### Production-Ready Recommendations
- Use replication factor 3 and `min.insync.replicas=2` for durable topics — see [Replication](Replication.md).
- Match consumer group size to partitions for full parallelism.

*References:*
- [Introduction to Apache Kafka](../summaries/001_introduction.md)
- [Apache Kafka and the File System](../summaries/006_kafka_file_system.md)
