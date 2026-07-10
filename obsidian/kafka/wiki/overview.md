# Apache Kafka Overview

Apache Kafka® is a distributed event streaming platform that handles high-throughput, real-time data feeds. It acts as a central nervous system for data infrastructure.

## Key Benefits & Design Goals
- **High Throughput & Scalability:** Handles millions of events per second and scales horizontally by adding brokers.
- **Handling Backlogs:** Gracefully deals with large data backlogs, supporting both real-time streaming and offline periodic loads.
- **Database Log Model:** Operates more like a persistent database log than a traditional transient message queue.
- **Durability & Replayability:** Uses an append-only log structure that stores data durably. Events are not deleted after consumption, allowing consumers to replay historical data.
- **Fault Tolerance:** Data is replicated across multiple brokers.
- **Decoupling:** Allows producers and consumers to operate independently.

## Common Use Cases
- Real-time data pipelines and stream processing.
- Event-driven microservices architectures.
- Activity tracking, log aggregation, and metric gathering.

## Core Concepts
- **[Topics & Partitions](concepts/Topics.md):** The fundamental unit of organization.
- **[Brokers](concepts/Brokers.md):** The servers forming the storage layer.
- **[Producers](concepts/Producers.md) & [Consumers](concepts/Consumers.md):** The clients that write and read data.
