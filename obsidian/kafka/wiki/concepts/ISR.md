# In-Sync Replicas (ISR)

**In-Sync Replicas (ISR, синхронные реплики)** are the set of partition replicas that are fully caught up with the leader. A message is **committed (закоммичено)** only when all ISR members have applied it, and only committed messages are visible to consumers. See [Replication](Replication.md).

- A follower stays in the ISR if it keeps its controller session and does not lag more than `replica.lag.time.max.ms`.
- **Quorum efficiency:** ISR needs only `f+1` replicas to tolerate `f` failures (vs. `2f+1` for majority vote); commit latency is bounded by the *slowest* ISR member.
- `min.insync.replicas` sets the minimum ISR required for an `acks=all` write; below it the leader returns `NotEnoughReplicasException`.
- A crashed replica must fully re-sync before rejoining the ISR (Kafka avoids mandatory `fsync`).

### Best Practices
- Standard: `replication.factor=3` + `min.insync.replicas=2` + producer `acks=all`.
- Alert on `UnderReplicatedPartitions`; a shrunk ISR endangers durability.

### Production-Ready Recommendations
- Lower `replica.lag.time.max.ms` to 10–15 s to evict stuck brokers faster.
- Isolate broker network/disk I/O — a slow ISR follower raises producer latency under `acks=all`.

*References:*
- [Kafka Replication (summary)](../summaries/013_kafka_replication.md)
- [Durability, Availability, Ordering](../summaries/011_kafka_durability_availability_ordering.md)
- [Replication](Replication.md)

