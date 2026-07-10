# Brokers

A **Broker (брокер / сервер)** is a server in the Kafka storage layer that stores event streams from one or more sources. 
- A Kafka cluster is composed of multiple brokers.
- Every broker is a bootstrap server (connecting to one allows connection to the entire cluster).
- Brokers store messages as a sequential log on the file system and serve reads from the OS page cache — see [File System & Page Cache](File_System_and_Page_Cache.md).
- Brokers expose **metadata (метаданные)** so clients route requests directly to partition leaders.

### Best Practices
- Leave ample free RAM for the OS page cache; keep JVM heaps modest.
- Use large, sequential-friendly disks (JBOD).

### Production-Ready Recommendations
- Size RAM so the recent working set stays in page cache.
- Avoid co-locating memory-hungry processes on broker hosts.
- Set `advertised.listeners` correctly for reachable client connections.

*References:*
- [Introduction to Apache Kafka](../summaries/001_introduction.md)
- [Apache Kafka and the File System](../summaries/006_kafka_file_system.md)
