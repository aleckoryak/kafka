# Quotas

**Quotas (квоты)** limit the broker resources a client group can consume, protecting multi-tenant clusters from DoS by badly-behaved clients.

- **Network bandwidth quotas (сетевые квоты):** byte-rate threshold (since 0.9).
- **Request rate quotas (квоты частоты запросов):** % of I/O + network thread time = CPU (since 0.11).
- Applied to `(user, client-id)` **client groups (клиентские группы)**; most specific match wins; connections share the quota.
- Quotas are **per-broker** and change dynamically (no restart).
- **Throttling (троттлинг):** on violation the broker delays responses and mutes the client channel (backpressure); measured over many small windows.

### Best Practices
- Set cluster-level `<default>` quotas; manage granularly per `(user, client-id)`; separate produce vs. fetch limits.
- Ensure clients handle throttling rather than aggressively reconnecting.

### Case Studies
- **Noisy-neighbor isolation**, **Kafka-as-a-Service billing/SLA**, **safe historical backfill** without saturating real-time streams.

### Production-Ready Recommendations
- Distribute partitions evenly (per-broker limits) so client load balances.
- Monitor `produce-throttle-time-max` / `fetch-throttle-time-max`; require client exponential backoff.

*References:*
- [Kafka Quotas](../summaries/016_kafka_quotas.md)
- [Kafka Security (AuthN/AuthZ)](../summaries/019_kafka_security_authn_authz.md)
- [Brokers](Brokers.md)

