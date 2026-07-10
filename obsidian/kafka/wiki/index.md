# Kafka Knowledge Base Index

Welcome to the Apache Kafka Wiki. This catalog indexes all ingested knowledge. Pages are multilingual (English/Russian/Ukrainian) and keep original terms with translations in brackets.

## Summaries
- [001. Introduction to Apache Kafka](summaries/001_introduction.md)
- [002. What is a Kafka Consumer and How does it work?](summaries/002_kafka_consumers.md)
- [003. What are Kafka Producers and How do they work?](summaries/003_kafka_producers.md)
- [004. Kafka Frequently Asked Questions](summaries/004_kafka_faq.md)
- [005. Kafka Design Overview](summaries/005_kafka_design_overview.md)
- [006. Apache Kafka and the File System](summaries/006_kafka_file_system.md)
- [007. Kafka Batch Processing for Efficiency](summaries/007_kafka_batching.md)
- [008. Kafka Producer Design](summaries/008_kafka_producer_design.md)
- [009. Kafka Consumer Design: Consumers, Consumer Groups, and Offsets](summaries/009_kafka_consumer_design.md)
- [010. Message Delivery Guarantees for Apache Kafka](summaries/010_kafka_delivery_guarantees.md)
- [011. Configuring Durability, Availability, and Ordering Guarantees](summaries/011_kafka_durability_availability_ordering.md)
- [012. Kafka Transactions & Exactly-Once Semantics](summaries/012_kafka_transactions_eos.md)
- [013. Kafka Replication and Committed Messages](summaries/013_kafka_replication.md)
- [014. Kafka Log Compaction](summaries/014_kafka_log_compaction.md)
- [015. Apache Kafka Topic Compaction (deep dive)](summaries/015_kafka_topic_compaction.md)
- [016. Kafka Quotas](summaries/016_kafka_quotas.md)
- [017. Message Delivery Semantics (with Java examples)](summaries/017_message_delivery_semantics.md)
- [018. Exactly-Once Semantics in Apache Kafka (Habr)](summaries/018_exactly_once_habr.md)
- [019. Kafka Authentication and Authorization](summaries/019_kafka_security_authn_authz.md)
- [020. When NOT to Use Apache Kafka](summaries/020_when_not_to_use_kafka.md)
- [021. Of Streams and Tables in Kafka (Part 1)](summaries/021_streams_and_tables.md)
- [022. Spring Boot Application Using Kafka Streams](summaries/022_spring_boot_kafka_streams.md)
- [023. Kafka in Practice — Cluster with docker-compose](summaries/023_kafka_cluster_docker_compose.md)

## Concepts
- [Batching](concepts/Batching.md) — message sets, zero-copy, compression, throughput tuning
- [Brokers](concepts/Brokers.md)
- [Consumers](concepts/Consumers.md)
- [Consumer Groups](concepts/Consumer_Groups.md) — group coordinator, rebalance protocols
- [Controller](concepts/Controller.md) — failure detection, leader election, KRaft
- [Dead Letter Queue](concepts/Dead_Letter_Queue.md) — poison pills, DLQ/DLT handling
- [Delivery Semantics](concepts/Delivery_Semantics.md) — at-most/at-least/exactly/effectively-once
- [File System & Page Cache](concepts/File_System_and_Page_Cache.md) — sequential I/O, O(1) log storage
- [In-Sync Replicas (ISR)](concepts/ISR.md) — committed messages, quorum, min.insync.replicas
- [Kafka Cluster & docker-compose](concepts/Kafka_Cluster_Docker.md) — practical multi-broker setup
- [Kafka Streams](concepts/Kafka_Streams.md) — processing topologies, EOS
- [Log Compaction](concepts/Log_Compaction.md) — key-based retention, tombstones
- [Offsets](concepts/Offsets.md) — committed offset, high watermark, lag
- [Partitions](concepts/Partitions.md)
- [Producers](concepts/Producers.md)
- [Quotas](concepts/Quotas.md) — bandwidth/request-rate limits, throttling
- [Replication](concepts/Replication.md)
- [Security](concepts/Security.md) — authentication (mTLS/SASL) & authorization (ACL/RBAC)
- [Streams and Tables](concepts/Streams_and_Tables.md) — stream-table duality, changelogs
- [Topics](concepts/Topics.md)
- [Transactions](concepts/Transactions.md) — atomic multi-partition writes, EOS
- [When Not to Use Kafka](concepts/When_Not_to_Use_Kafka.md) — limits and complements

