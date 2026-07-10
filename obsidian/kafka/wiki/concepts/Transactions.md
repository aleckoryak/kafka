# Transactions

**Transactions (транзакции)** in Kafka provide database-style all-or-nothing atomic writes across multiple partitions/topics, plus the consumer offset commit — the basis for **Exactly-Once Semantics (EOS)**. See [Delivery Semantics](Delivery_Semantics.md).

- **Transaction coordinator (координатор транзакций):** broker component (leader of an internal transaction-state topic partition) that issues a **Producer ID + epoch (эпоха)** and records participating partitions.
- **Fencing (ограждение зомби):** on restart the coordinator aborts the old instance's pending transactions and bumps the epoch, blocking zombie producers.
- **Last Stable Offset (LSO):** the first open pending transaction; `read_committed` consumers read only up to it and skip aborted records.
- Enabled via Kafka Streams `processing.guarantee=exactly_once(_v2)` or a producer `transactional.id`; automatically turns on idempotence.

### Best Practices
- Full EOS quartet: `acks=all` + `enable.idempotence=true` + `transactional.id` + consumer `isolation.level=read_committed`.
- Balance commit interval (default 100 ms): too frequent = overhead, too large = delayed visibility.

### Case Studies
- **Fund transfer (Alice→Bob):** debit + credit + offset commit in one transaction; a crash rolls back, avoiding double debit.

### Production-Ready Recommendations
- Kafka has **no 2PC** to external systems — make transactional output idempotent and apply to external DBs separately (or use Transactional Outbox).
- Prefer `exactly_once_v2` for lower overhead.

*References:*
- [Kafka Transactions & EOS](../summaries/012_kafka_transactions_eos.md)
- [Exactly-Once Semantics (Habr)](../summaries/018_exactly_once_habr.md)
- [Delivery Semantics](Delivery_Semantics.md)
- [Kafka Streams](Kafka_Streams.md)

