# Delivery Semantics

**Delivery Semantics (семантики доставки)** describe the guarantees Kafka provides for sharing messages between producers, brokers, and consumers. They trade latency against durability.

- **At most once (максимум один раз):** messages may be lost, never duplicated.
- **At least once (минимум один раз):** never lost, may be duplicated — the **default**.
- **Exactly once / EOS (строго один раз):** never lost, never duplicated.
- **Effectively once (эффективно один раз):** an architectural pattern — run at-least-once but make the consumer idempotent (track processed IDs / upsert by business key) so duplicates are harmless.
- A message is **committed (закоммичено)** once written and safe while one in-sync replica lives.
- **Idempotence (идемпотентность):** PID + sequence number removes duplicates on retries (since 0.11, default on).
- **Transactions (транзакции):** enable exactly-once read-process-write with consumer `isolation.level=read_committed` — see [Transactions](Transactions.md).

### Best Practices
- At-least-once requires `acks=all`, not `acks=1` (a leader crash before replication under `acks=1` silently loses data).
- Exactly-once quartet: `acks=all` + `enable.idempotence=true` + transactional producer (`transactional.id`) + consumer `read_committed`.
- `min.insync.replicas` only applies with `acks=all`.

### Case Studies
- **Banking transfer:** debit + credit + offset commit in one transaction; a crash rolls back, avoiding lost/duplicate money.
- **Retailer network storm:** without idempotence, retried batches were written 2–3×; `enable.idempotence=true` resolved it.
- **Auto-commit loss:** default `enable.auto.commit=true` skipped unprocessed payments after a crash; manual `commitSync()` fixed it.

### Production-Ready Recommendations
- Disable auto-commit; commit manually after processing each `poll()` batch.
- Make sinks idempotent (upserts / deterministic document IDs / Transactional Outbox).
- Use `acks=all` + idempotence + transactions for critical data; `acks=0` only for lossy telemetry.
- Route poison-pill messages to a [Dead Letter Queue](Dead_Letter_Queue.md) to avoid blocked consumers.

*References:*
- [Message Delivery Guarantees](../summaries/010_kafka_delivery_guarantees.md)
- [Message Delivery Semantics (Java examples)](../summaries/017_message_delivery_semantics.md)
- [Exactly-Once Semantics (Habr)](../summaries/018_exactly_once_habr.md)
- [Transactions](Transactions.md)
- [Dead Letter Queue](Dead_Letter_Queue.md)
- [Replication](Replication.md)
- [Offsets](Offsets.md)
- [Producers](Producers.md)

