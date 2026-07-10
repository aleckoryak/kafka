# Summary: When NOT to Use Apache Kafka

**Source:** `raw/Когда НЕ нужно использовать Apache Kafka.md` (RU, Habr — translation of Kai Waehner)
**Source URL:** https://habr.com/ru/companies/mws/articles/725686/
**Date Ingested:** 2026-07-09

## Key Takeaways
- Kafka **is:** a scalable real-time messaging platform, an event-streaming platform, distributed storage, a data-integration framework (Kafka Connect), and a processing framework (Kafka Streams/ksqlDB).
- Kafka is **NOT:** a proxy for millions of clients, an API management platform, a database for complex queries/batch analytics, an IoT device-management platform, or a hard-real-time/deterministic system.
- **Kafka is not hard real-time (не строго реального времени):** it is *soft* real-time (ms–s). Sub-millisecond needs (HFT, engine control, pacemakers, robotics, V2V) require other tech.
- **Not for bad networks (плохие сети):** needs stable client↔broker connectivity; use MQTT at the edge (Kafka + MQTT are friends).
- **Not tens of thousands of clients:** front with an HTTP/MQTT proxy or REST Proxy; direct hybrid connections work well up to hundreds of clients.
- **Usually not a database replacement, not for large messages (>1 MB), not an IoT gateway, not a blockchain** (but relevant for web3/off-chain/oracles).

### Best Practices
- Choose the right tool per task; Kafka complements (not competes with) databases, MQTT, API gateways, blockchains.
- For large payloads, store the file elsewhere and send a reference + metadata (claim-check pattern).

### Case Studies
- **Success:** Tesla (millions of devices, trillions of points/day), BMW (smart factories + SAP/ERP), Royal Caribbean (edge cluster per ship), Unity (migrated to Confluent Cloud with zero downtime), Audi, Disney+ Hotstar.
- **Complement patterns:** MQTT + Kafka + TensorFlow for 100,000 connected cars; PLC4X + Kafka Connect for legacy industrial protocols (Siemens S7, Modbus).

### Production-Ready Recommendations
- Use multi-region/multi-cluster deployments for HA, disaster recovery, and global Kafka.
- For edge/unstable links, deploy autonomous edge Kafka or an MQTT bridge.
- Build materialized views in specialized stores (Elasticsearch, Snowflake) fed from a central Kafka event layer.

## Concepts Covered
- [When Not to Use Kafka](../concepts/When_Not_to_Use_Kafka.md)
- [Kafka Streams](../concepts/Kafka_Streams.md)

