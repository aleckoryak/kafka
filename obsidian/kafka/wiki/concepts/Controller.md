# Controller

The **Controller (контроллер)** is the broker responsible for cluster coordination: detecting broker failures and electing new partition leaders. Leadership-change notifications are **batched (пакетно)** so election is fast even for thousands of partitions. If the controller fails, a surviving broker takes over.

- Balances partitions and leadership across brokers (round-robin) to avoid hot brokers.
- In **KRaft (Kafka Raft)** the controller role is integrated into Kafka's metadata quorum, replacing ZooKeeper and cutting failover time to milliseconds.

### Best Practices
- Keep `auto.leader.rebalance.enable=true` so preferred leaders return after broker restarts.
- In KRaft, use dedicated controller nodes (no user data) for large clusters.

### Production-Ready Recommendations
- Migrate to **KRaft** to scale to millions of partitions with fast failover.
- Alert on `ActiveControllerCount` (must equal 1 — 0 = headless, >1 = split-brain), `OfflinePartitionsCount`, `LeaderElectionRateAndTimeMs`.

*References:*
- [Kafka Replication (summary)](../summaries/013_kafka_replication.md)
- [Kafka Cluster & docker-compose](../summaries/023_kafka_cluster_docker_compose.md)
- [Replication](Replication.md)

