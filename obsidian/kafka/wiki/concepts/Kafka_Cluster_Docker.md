# Kafka Cluster & docker-compose

A **Kafka Cluster (кластер Kafka)** is multiple [Brokers](Brokers.md) coordinated (via ZooKeeper or KRaft) with a [Controller](Controller.md). This page captures a practical local cluster via `docker-compose`.

- Typical stack: ZooKeeper + 2 brokers (`KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=2`, distinct `KAFKA_BROKER_ID`, `KAFKA_ADVERTISED_LISTENERS`) + Kafka UI (provectus) + producer/consumer services.
- **Partition selection:** keyed → `hash(key) % partitions`; keyless → **Sticky Partitioner** (round-robin batching).
- **Consumer-group parallelism:** one thread reads one partition; each group receives all messages independently; replication factor requires ≥ that many brokers.

### Best Practices
- Threads must not be fewer than partitions and vice versa (one thread per partition); partition count can only increase.
- A [Dead Letter Queue](Dead_Letter_Queue.md) topic (`*.dlt`) must have the same partition count as its source topic.

### Case Studies
- 10,000 messages with 2 consumer groups → each processed twice; `number % 100 == 0` throws → 200 messages routed to `topic1.dlt`.

### Production-Ready Recommendations
- Use Kafka UI to inspect brokers, controller, partitions, and replica sync.
- Set RF only up to the broker count; use `advertised.listeners` reachable by clients.

*References:*
- [Kafka Cluster & docker-compose (summary)](../summaries/023_kafka_cluster_docker_compose.md)
- [Controller](Controller.md)
- [Consumer Groups](Consumer_Groups.md)
- [Dead Letter Queue](Dead_Letter_Queue.md)

