# Replication

**Replication (репликация)** ensures high availability and fault tolerance by keeping multiple copies of topic partitions across different brokers.
- A common replication factor is 3.
- A message is **committed (закоммичено)** once written to the log and stays safe as long as one in-sync replica (ISR) remains alive — see [In-Sync Replicas (ISR)](ISR.md).
- **`min.insync.replicas`** sets the minimum ISR that must acknowledge a write under `acks=all`; below it the leader rejects writes with `NotEnoughReplicasException`.
- Leader election and failure handling are coordinated by the [Controller](Controller.md); **unclean leader election** trades consistency for availability (`unclean.leader.election.enable`, default `false`).

### Best Practices
- Use replication factor 3 with `min.insync.replicas=2` for durable topics.
- Pair replication with producer `acks=all` — `acks=1` can lose data if the leader dies before replication.

### Production-Ready Recommendations
- Spread replicas across racks/availability zones to survive correlated failures.
- Monitor under-replicated partitions and ISR shrink/expand events.
- Combine with idempotence/transactions for exactly-once durability — see [Delivery Semantics](Delivery_Semantics.md).

*References:*
- [Introduction to Apache Kafka](../summaries/001_introduction.md)
- [Message Delivery Guarantees](../summaries/010_kafka_delivery_guarantees.md)
- [Durability, Availability, Ordering](../summaries/011_kafka_durability_availability_ordering.md)
- [Kafka Replication (summary)](../summaries/013_kafka_replication.md)
- [In-Sync Replicas (ISR)](ISR.md)
- [Controller](Controller.md)
