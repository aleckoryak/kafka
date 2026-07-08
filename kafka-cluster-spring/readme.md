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

| Topic        | Partitions | Replication factor |
|--------------|------------|--------------------|
| `topic1`     | 4          | 2                  |
| `topic1.dlt` | 4          | 2                  |

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

- **Scenario 1** – 1 broker, 4 partitions, RF 1, one consumer group with two
  consumers splitting the partitions.
- **Scenario 2** – 1 broker, keyless messages using the Sticky Partitioner,
  two consumer groups.
- **Scenario 3** – 2 brokers, RF 2 (each partition has a replica), two consumer
  groups with one consumer each. **This is the setup reproduced here.**

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
- Publish 10,000 messages:

```powershell
# Docker
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/messages/publish?count=10000"

# Podman (same command — it exposes the same host ports)
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/messages/publish?count=10000"
```

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

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-26.0.1"
C:\software\gradle\gradle-9.6.1\bin\gradle.bat :kafka-cluster-spring:producer:bootRun
C:\software\gradle\gradle-9.6.1\bin\gradle.bat :kafka-cluster-spring:consumer:bootRun
```

The apps default to `localhost:9092,localhost:9094` (the brokers' EXTERNAL
listeners) when `SPRING_KAFKA_BOOTSTRAP_SERVERS` is not set.

### IntelliJ IDEA — Gradle JVM setting

Go to **Settings → Build → Build Tools → Gradle** and set:
- **Gradle JVM**: `C:\Program Files\Java\jdk-26.0.1`
- **Use Gradle from**: `Specified location` → `C:\software\gradle\gradle-9.6.1`

This avoids the "incompatible Java / Gradle" error that occurs when IntelliJ
uses its bundled Gradle (9.3.0, max Java 25) instead of the local 9.6.1.

