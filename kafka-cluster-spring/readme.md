# kafka-cluster-spring

A Spring Boot demonstration of an Apache **Kafka cluster running in KRaft mode
(no ZooKeeper)**, adapted from the Habr article
["Работа Apache Kafka на примерах"](https://habr.com/ru/articles/738874/).

The project shows how producers, brokers, partitions, consumer groups and a
Dead Letter Topic (DLT) work together. Everything (2 brokers + Kafka UI +
producer + consumer) is started from a single `docker-compose.yml`.

## Modules

| Module     | Port | Description                                                        |
|------------|------|--------------------------------------------------------------------|
| `producer` | 8080 | REST endpoint that publishes JSON messages to the `topic1` topic.  |
| `consumer` | 8081 | Two listeners in different consumer groups, 4 threads, DLT routing.|

- Group / package: `com.ok.kafka-cluster-spring` / `com.ok.kafka.cluster.*`
- Java 21, Spring Boot 3.5.x, Spring Kafka
- Kafka 4.x brokers (KRaft only)

## Topics
Topics are created automatically at producer startup via `NewTopic` beans.
The DLT has the same partition count as the source topic, because the DLT
publisher preserves the original partition of a failed record.

## How it works

1. The producer sends keyed JSON messages. The key hash decides the partition.
2. Two consumers live in **different** consumer groups, so every message is
   read twice.
3. Consumption runs on **4 threads** (one per partition).
4. Any message whose `number` is a multiple of 100 throws an exception and is
   routed to `topic1.dlt`. For 10,000 messages read by 2 groups this yields
   `10000 / 100 * 2 = 200` DLT records.

## Scenarios from the article

### Scenario 1 — 1 broker · 1 consumer group · keyed messages

| Brokers | Partitions | RF | Producer | Consumer groups | Threads/group |
|---------|------------|----|----------|-----------------|---------------|
| 1       | 4          | 1  | keyed    | **1**           | 2             |

- The producer sends messages with a random integer key. Kafka hashes the key
  to decide which partition receives the message.
- One consumer group has **2 threads**: each thread reads 2 of the 4 partitions.
- If one thread fails, the surviving thread takes over all 4 partitions.
- `replication-factor=1` → only a master partition exists (no replica), so a
  broker failure means data loss.

```powershell
docker compose -f docker-compose.scenario1.yml up --build
# then:
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/messages/publish?count=10000"
```

**What to observe in Kafka UI (`http://localhost:8090`):**
- Topic `topic1`: 4 partitions, messages distributed by key hash.
- Consumer group `consumer-group-1`: 2 active members, each assigned 2 partitions.
- Topic `topic1.dlt`: ~100 messages (every 100th × 1 group).

---

### Scenario 2 — 1 broker · 2 consumer groups · keyless / Sticky Partitioner

| Brokers | Partitions | RF | Producer   | Consumer groups | group-1 threads | group-2 threads |
|---------|------------|----|------------|-----------------|-----------------|-----------------|
| 1       | 4          | 1  | **keyless**| 2               | 2               | **3**           |

- The producer sends messages **without a key**. Kafka's Sticky Partitioner fills
  one batch per partition in round-robin order, giving an even distribution.
- **group-1** has 2 threads: each reads 2 partitions.
- **group-2** has 3 threads: one reads 2 partitions, two read 1 each.
- Both groups receive all 10 000 messages → 20 000 total reads.

```powershell
docker compose -f docker-compose.scenario2.yml up --build
# then:
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/messages/publish?count=10000"
```

**What to observe:**
- Topic `topic1`: partitions filled evenly (Sticky Partitioner).
- Two consumer groups listed; different partition assignments per group.
- Topic `topic1.dlt`: ~200 messages (every 100th × 2 groups).

---

### Scenario 3 — 2 brokers · 2 consumer groups · keyed messages · RF=2 ✅ (default)

| Brokers | Partitions | RF  | Producer | Consumer groups | Threads/group |
|---------|------------|-----|----------|-----------------|---------------|
| **2**   | 4          | **2** | keyed  | 2               | 4             |

- Two brokers allow `replication-factor=2`: every partition has a master on one
  broker and a replica on the other. The replica takes over if the master fails.
- Producer sends keyed messages; Kafka routes by key hash.
- Each consumer group has **4 threads** — one thread per partition, maximum
  parallelism.
- Both groups receive all messages → 20 000 total reads.

```powershell
docker compose up --build          # uses docker-compose.yml (default)
# then:
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/messages/publish?count=10000"
```

**What to observe:**
- Two brokers in Kafka UI; controller elected from either.
- Topic `topic1`: 4 partitions distributed across 2 brokers, each with a replica.
- Two consumer groups, 4 active members each.
- Topic `topic1.dlt`: ~200 messages (every 100th × 2 groups).
- Stop one broker container and watch Kafka elect new partition leaders.

---

### Configuration reference

All scenario settings are controlled by environment variables (overriding
`application.yml`). The table below shows what each compose file sets:

| Property (env var)                    | Scenario 1 | Scenario 2 | Scenario 3 |
|---------------------------------------|-----------|-----------|-----------|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS`      | `kafka1:9092` | `kafka1:9092` | `kafka1:9092,kafka2:9092` |
| `KAFKA_TOPICS_REPLICATION_FACTOR`     | **1**     | **1**     | 2         |
| `KAFKA_TOPICS_PARTITIONS`             | 4         | 4         | 4         |
| `KAFKA_PRODUCER_USE_KEYS`             | `true`    | **false** | `true`    |
| `KAFKA_CONSUMER_GROUP1_CONCURRENCY`   | **2**     | **2**     | 4         |
| `KAFKA_CONSUMER_GROUP2_ENABLED`       | **false** | true      | true      |
| `KAFKA_CONSUMER_GROUP2_CONCURRENCY`   | —         | **3**     | 4         |
| Compose file                          | `docker-compose.scenario1.yml` | `docker-compose.scenario2.yml` | `docker-compose.yml` |

## Run with Docker

```powershell
cd kafka-cluster-spring
docker compose up --build
```

#### Equivalent with Podman

```powershell
cd kafka-cluster-spring
podman compose up --build
```

> **Note:** Podman Desktop ships `podman compose` as a wrapper around
> `docker-compose` (Python). If it is not installed run
> `pip install podman-compose` first, or use the alias
> `podman-compose up --build`.

- Kafka UI: <http://localhost:8090>


Then watch the consumer logs and inspect `topic1` and `topic1.dlt` in Kafka UI.

## Stop & clean up

```powershell
# Docker
docker compose down -v

# Podman
podman compose down -v
```

## Run locally (without Docker / Podman)

**Prerequisites (local machine)**

| Tool   | Path                                    | Version |
|--------|-----------------------------------------|---------|
| JDK    | `C:\Program Files\Java\jdk-26.0.1`     | 26.0.1  |
| Gradle | `C:\software\gradle\gradle-9.6.1`      | 9.6.1   |

Start only the brokers + UI:

```powershell
# Docker — brokers + UI only
docker compose up kafka1 kafka2 kafka-ui

# Podman — brokers + UI only
podman compose up kafka1 kafka2 kafka-ui
```

Then start the apps. Use the **Gradle wrapper** (recommended — picks up
`gradle-wrapper.properties` automatically):

```powershell
# from repo root — Windows wrapper
.\gradlew.bat :kafka-cluster-spring:producer:bootRun
.\gradlew.bat :kafka-cluster-spring:consumer:bootRun
```

Or use the local Gradle installation directly:

```bash
C:\software\gradle\gradle-9.6.1\bin\gradle.bat :kafka-cluster-spring:producer:bootRun
```
- Publish 10,000 messages:

```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/messages/publish?count=10000"
```

```bash
curl.exe -X POST "http://localhost:8080/messages/publish?count=100"
```

```bash
C:\software\gradle\gradle-9.6.1\bin\gradle.bat :kafka-cluster-spring:consumer:bootRun
```

The apps default to `localhost:9092,localhost:9094` (the brokers' EXTERNAL
listeners) when `SPRING_KAFKA_BOOTSTRAP_SERVERS` is not set.


