# Summary: Kafka Quotas

**Source:** `raw/016. Kafka Quotas.md`
**Source URL:** https://docs.confluent.io/kafka/design/quotas.html
**Date Ingested:** 2026-07-09

## Key Takeaways
- **Quotas (квоты)** limit broker resources a client group can use, protecting multi-tenant clusters from DoS by badly-behaved clients.
- Two types: **network bandwidth quotas (сетевые квоты)** (byte-rate, since 0.9) and **request rate quotas (квоты частоты запросов)** (% of I/O + network thread time = CPU, since 0.11).
- **Client groups (клиентские группы):** quotas apply to `(user, client-id)` tuples; the most specific match wins, and all connections in a group share the quota.
- Quotas are **per-broker** and can be changed dynamically (no restart).
- **Enforcement (throttling):** on violation the broker computes a delay, returns an empty/delayed response, and mutes the client channel; well-behaved clients also pause. Measured over many small windows (e.g. 30×1s) for quick correction.

### Best Practices
- Always set cluster-level `<default>` quotas to protect against new/misconfigured clients.
- Manage granularly per `(user, client-id)`; set produce and fetch limits separately.
- Ensure clients handle throttling (backpressure) instead of aggressively reconnecting.

### Case Studies
- **Noisy-neighbor isolation:** a buggy microservice refetching terabytes is contained so payment processing stays unaffected.
- **Kafka-as-a-Service billing:** cloud providers enforce SLA/API limits per plan via quotas.
- **Safe historical backfill:** a new analytics consumer can't saturate bandwidth against real-time streams.

### Production-Ready Recommendations
- Because limits are per-broker, distribute partitions evenly so a client's load balances across the cluster.
- Monitor JMX `produce-throttle-time-max` / `fetch-throttle-time-max` and alert when clients hit limits.
- Require exponential backoff on clients after throttling to avoid post-window load spikes.

### Diagrams
```plantuml
@startuml
title Quota Throttling Flow
actor Client
node Broker
Client -> Broker : produce/fetch requests
Broker -> Broker : measure byte-rate / thread% over windows
alt quota exceeded
  Broker -> Broker : compute delay, mute channel
  Broker --> Client : delayed/empty response
  Client -> Client : pause for delay (backpressure)
else within quota
  Broker --> Client : normal response
end
@enduml
```

## Concepts Covered
- [Quotas](../concepts/Quotas.md)
- [Brokers](../concepts/Brokers.md)
- [Security](../concepts/Security.md)

