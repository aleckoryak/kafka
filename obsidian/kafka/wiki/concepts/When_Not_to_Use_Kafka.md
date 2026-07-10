# When Not to Use Kafka

**When Not to Use Kafka (когда НЕ нужно использовать Kafka)** captures the limits of Apache Kafka so it is complemented, not misapplied. Kafka is a scalable soft-real-time event-streaming platform, distributed storage, and integration/processing framework — but it is not a universal solution.

Kafka is **NOT**:
- **Hard real-time (строго реального времени):** it is soft real-time (ms–s); sub-ms needs (HFT, engine control, pacemakers, robotics, V2V) require other tech.
- **For bad networks (плохие сети):** needs stable client↔broker links; use MQTT at the edge (Kafka + MQTT are friends).
- **A proxy for tens of thousands of clients:** front with HTTP/MQTT proxy or REST Proxy; direct connections scale to hundreds.
- **A database replacement**, **for large messages (>1 MB)**, **an IoT device-management gateway**, an **API management platform**, or a **blockchain** (though relevant for web3/off-chain/oracles).

### Best Practices
- Choose the right tool per task; Kafka complements databases, MQTT, API gateways, and blockchains.
- For large payloads use the **claim-check** pattern (store file elsewhere, send a reference + metadata).

### Case Studies
- **Successes:** Tesla, BMW (+SAP/ERP), Royal Caribbean (edge per ship), Unity (zero-downtime cloud migration), Disney+ Hotstar.
- **Complements:** MQTT + Kafka + TensorFlow for 100,000 connected cars; PLC4X + Kafka Connect for legacy industrial protocols.

### Production-Ready Recommendations
- Use multi-region/multi-cluster for HA, DR, and global Kafka.
- Deploy autonomous edge Kafka or an MQTT bridge for unstable links.
- Build materialized views in specialized stores (Elasticsearch, Snowflake) fed from a central Kafka event layer.

*References:*
- [When NOT to Use Kafka (summary)](../summaries/020_when_not_to_use_kafka.md)
- [Kafka Streams](Kafka_Streams.md)

