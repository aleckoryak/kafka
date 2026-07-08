---
title: "Работа Apache Kafka на примерах. Поднимаем Kafka Cluster используя docker-compose"
source: "https://habr.com/ru/articles/738874/"
author:
  - "[[maks-java]]"
published: 2023-05-31
created: 2026-07-08
description: "О чём? Сегодня я постараюсь продемонстрировать\объяснить работу Kafka, используя как можно меньше определений и побольше практики. Мы рассмотрим 3 сценария работы с Kafka...."
tags:
  - "clippings"
---
### О чём?

Сегодня я постараюсь продемонстрировать\\объяснить работу Kafka, используя как можно меньше определений и побольше практики. Мы рассмотрим 3 сценария работы с Kafka. Для последнего сценария мы поднимем Kafka Cluster в Docker и с помощью UI увидим, как происходит общение между сервисами.

### Теория

- **Kafka** — брокер сообщений, с помощью которого разные микросервисы общаются друг с другом. Также используется для отправки логов (например в Graylog и Elastic), хранения данных и т. д.
- **Брокер** — узел Kafka, отвечает за прием, сохранение и передачу сообщений между продюсерами (Producer) и консюмерами (Consumer)
- **Консюмер** (Consumer) — получатель сообщения
- **Продюсер** (Producer) — отправитель сообщения
- **Zookepeer** — хранит конфигурации, состояния, обнаруживает брокеров, выбирает контроллер кластера, отслеживает состояние узлов и обеспечивает функциональность и надёжность кластера
- **Контроллер** **кластера** — отвечает за назначение мастеров партиций и отслеживает состояние брокеров
- **Offset** **Kafka** — это понятие, которое используется для обозначения позиции в потоке сообщений. Offset отслеживает, на каком месте в потоке находится каждый консюмер, чтобы он мог читать сообщения с нужной позиции.  
	Каждый раз, когда потребитель читает сообщение, его позиция сдвигается на одно сообщение вперед.
- **Партиция** (Particion) — единица многопоточности в Kafka. Число партиций в топике можно лишь увеличивать. Партиции это те самые шарды, которые используются для шардирования в Kafka.
- **Реплика** (Replica) — копия партиции. Реплика может быть размещена на другом узле для обеспечения отказоустойчивости. Обеспечивает репликацию данных.
- **Топик** (topic) — служит для записи и чтения сообщений.
- **Консюмер** **группа** (Consumer group) — группа получателей сообщений

### Семантики

**At most once:** В этой семантике продюсер считает сообщение успешно доставленным, как только оно отправлено брокеру, независимо от его фактической доставки потребителю. Это означает, что сообщения могут быть потеряны, если брокер не смог доставить их потребителям. Эта семантика обеспечивает максимальную пропускную способность, но не гарантирует доставку сообщений.

**At least once:** В этой семантике продюсер ждет подтверждения (ACK) от брокера о доставке сообщения. Если продюсер не получает подтверждения в течение заданного времени, он повторно отправляет сообщение. Это гарантирует, что сообщения будут доставлены, но может привести к дублированию сообщений в случае сбоев и повторных отправок.

**Exactly once:** В этой семантике гарантируется, что каждое сообщение будет доставлено ровно один раз, без дублирования. Продюсер отправляет сообщения в рамках транзакции, и брокер подтверждает их только после успешной записи в журнал и передачи потребителям. Эта семантика обеспечивает наивысший уровень гарантий, но требует дополнительных механизмов и может снижать пропускную способность.

Семантика Exactly once очень дорогая и влечёт вместе с собой определенные риски. Так например, мы можем получить блокировку при отправке, поскольку данные могут не реплицироваться из‑за упавшей реплики или же уже после обработки сообщения, может произойти непредвиденная ситуация, извещение о доставке не будет получено и случится дубликат. Стремится к ней не стоит.

### Сценарии использования

Рассмотрим 3 сценария работы с Kafka. Также в каждом сценарии мы рассмотрим способы выбора партиции для сообщения.

#### Сценарий 1

У нас есть 1 продюсер, 1 брокер, 4 партиции и параметр replica‑set (отвечающий за репликацию) равный единице. Мы не можем поставить replica‑set больше числа брокеров. Таким образом у нас будет только мастер‑партиция. Также у нас будет 1 консюмер группа с двумя консюмерами.

Итак, наш продюсер отправляет 10 000 сообщений со случайными ключами. В таком случае в зависимости от ключа, который является целочисленным значением вычисляется хэш и определяется партиция в которую оно попадёт. Например, сообщение с ключом 12 при 4 партициях попадёт в 3 партицию и т. д.

У нас есть одна консюмер группа, которой достанутся все сообщения. В ней два консюмера и они распределяет между собой все сообщения. Zookepeer определит какой консюмер будет читать из какой партиции. При нашей конфигурации каждый консюмер будет читать из двух партиций. В случае падения одного из консюмеров все партиции будут писать в оставшийся консюмер. В случае падении одной из партиции один консюмер будет читать две партиции, а один — одну.

![Сценарий 1](https://habrastorage.org/r/w1560/getpro/habr/upload_files/27b/742/ddc/27b742ddc2a8783f9a0d85971506d561.png)

Сценарий 1

#### Сценарий 2

У нас есть 1 брокер, 1 продюсер, 4 партиции, 2 консюмер группы и 5 консюмеров распределённых по консюмер группам.

Продюсер отправляет 10 000 сообщений без ключей. В таком случае будет использован механизм Sticky Partitioner, который наполняет пакет для каждой партиции по отдельности и, как только пакет будет заполнен, он отправится на брокер. Это обеспечивает равномерное распределение сообщений между партициями и снижает задержку всей системы. Распределение между партициями происходит по кругу, т. е. сначала пакет идёт в первую, затем — во вторую и т. д. После этого партиции распределяется по консюмер группам.

Каждая консюмер группа будет получать все сообщения, который попадают в брокер. В первой консюмер группе у нас 2 консюмера, и каждый консюмер читает по 2 партиции. Во второй группе консюмер 3, т. е. один из консюмеров читает 2 партиции, а два оставшихся по одной. Таким образом 10 000 сообщений будет прочитано в обоих консюмер группах.

![Сценарий 2](https://habrastorage.org/r/w1560/getpro/habr/upload_files/274/f12/6d8/274f126d88fdb53474ef130c75c6feec.png)

Сценарий 2

#### Сценарий 3

У нас есть 2 брокера, 1 продюсер, 4 партиции, 2 консюмер группы и 2 консюмера распределённых по консюмер группам.

Продюсер отправляет 10 000 сообщений и для каждого сообщения используя ключ определяет в какую партицию оно попадёт. В нашем случае у нас 4 партиции, которые распределены по двум брокерам. Из‑за наличия двух брокеров мы смогли установить replica‑set равным двум, и теперь у каждой партиции есть копия, которая ждёт пока отвалится мастер, чтобы сменить её. Копия получает все изменения с мастера. Консюмер группы две, в каждой по одному консюмеру, следовательно каждый консюмер получает все сообщения.

![Сценарий 3](https://habrastorage.org/r/w1560/getpro/habr/upload_files/133/c10/aba/133c10abadec159f8d5c81c51364a53d.png)

Сценарий 3

#### Проект

Теперь перейдём к проекту. Подготовим отправителя и получателя для работы с Kafka. Начнём с продюсера. Для него будет использована автоконфигурация Spring

```yaml
spring:
  kafka:
    # Перечисляем всех наших брокеров
    bootstrap-servers: host.docker.internal:29092,host.docker.internal:29093
    producer:
      # Настраиваем сериализацию сообщений
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer

kafka:
  topics:
    test-topic: topic1Объяснить с
```

Отправлять будем дтошку с двумя полями number и message

```java
public class JsonMessage {

    private long number;

    private String message;

}Объяснить с
```

Отправку будем осуществлять через KafkaTemplate. Более подробное описание класса по отправке сообщения доступно на [GitHub](https://github.com/MarMaksk/kafka-cluster).

### Конфигурация Consumer

Консюмер сконфигурирован уже более интересно. С помощью нашей конфигурации мы сможем десериализовать только объекты определённого типа. Также мы создадим DLT (Dead Letter Publishing) очередь куда будут отправлены сообщения обработка которых прошла безуспешно (десериализация или уже непосредственно в бизнес логике).

```yaml
spring:
  main:
    allow-bean-definition-overriding: true
  application:
    name: kafka-example-consumer
  kafka:
    # Адреса всех брокеров кластера
    bootstrap-servers: host.docker.internal:29092,host.docker.internal:29093
    listener:
      # Получение каждой записи должно быть подтверждено
      ack-mode: record
    producer:
      client-id: ${spring.application.name}
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      # Очередь для исключений
      value-serializer: ru.home.kafka.config.DltMessageSerializer
      retries: 3
    consumer:
      group-id: ${spring.application.name}
      autoOffsetReset: earliest
      # Сериализаторы для всех типов
      keyDeserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
      valueDeserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
    properties:
      # Тип, для которого определяем конкретные сериализаторы
      spring.json.value.default.type: ru.home.kafka.dto.JsonMessage
      spring.deserializer.key.delegate.class: org.apache.kafka.common.serialization.StringDeserializer
      spring.deserializer.value.delegate.class: org.springframework.kafka.support.serializer.JsonDeserializer

kafka:
  topics:
    test-topic: topic1
Объяснить с
```

Создадим класс для конфигурирования нашего консюмера и отправителя в DLT очередь. Также мы сконфигурируем ConcurrentKafkaListenerContainerFactory с помощью которой мы сможем считывать сообщения из топика в несколько потоков (4 для нашей конфигурации). Важно учитывать, что число потоков не должно быть меньше числа партиций, а число партиций не должно быть меньше числа потоков. Лишь один поток может считывать из одной партиции. Необходимо помнить, что число партиций уменьшить нельзя:

```java
@Configuration
@EnableKafka
@RequiredArgsConstructor
public class KafkaConfiguration {

    private static final String DLT_TOPIC_SUFFIX = ".dlt";

    private final ProducerFactory<Object, Object> producerFactory;
    private final ConsumerFactory<Object, Object> consumerFactory;

    @Bean
    public KafkaTemplate<Object, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            DefaultErrorHandler errorHandler
    ) {
        // Позволяет создавать консюмеров, которые могут обрабатывать сообщения из нескольких партиций Kafka одновременно,
        // а также настраивать параметры такие как количество потоков, хэндлинг и т.д.
        ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory = new ConcurrentKafkaListenerContainerFactory<>();
        // Настройка фабрики для создания консьюмера Kafka
        kafkaListenerContainerFactory.setConsumerFactory(consumerFactory);
        // Возврат сообщений в DLT очередь
        kafkaListenerContainerFactory.setCommonErrorHandler(errorHandler);
        // Обработка сообщений в 4 потока
        kafkaListenerContainerFactory.setConcurrency(4);
        return kafkaListenerContainerFactory;
    }

    /**
     * Публикатор в dead-letter topic.
     */
    @Bean
    public DeadLetterPublishingRecoverer publisher(KafkaTemplate<Object, Object> bytesTemplate) {
        //  Определяем логику выбора партиции для отправки сообщения в DLT.
        //  В данном случае, создаём новый объект TopicPartition, используя имя топика (consumerRecord.topic()) и добавляя суффикс DLT_TOPIC_SUFFIX,
        //  а также номер партиции (consumerRecord.partition()).
        //  Следовательно в DLT топике должно быть столько партиций сколько и в топике откуда читаем
        return new DeadLetterPublishingRecoverer(bytesTemplate, (consumerRecord, exception) ->
                new TopicPartition(consumerRecord.topic() + DLT_TOPIC_SUFFIX, consumerRecord.partition()));
    }

    /**
     * Обработчик исключений при получении сообщений из kafka по умолчанию.
     */
    @Bean
    public DefaultErrorHandler errorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        final var handler = new DefaultErrorHandler(deadLetterPublishingRecoverer);
        // Обрабатываем любые исключения и отправляем в DLT
        handler.addNotRetryableExceptions(Exception.class);
        return handler;
    }
}Объяснить с
```

Напишем кастомный сериализатор для сообщений отправляемых в DLT очередь. Мы будет отправлять сообщения формата JSON:

```java
public class DltMessageSerializer<T> implements Serializer<T> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Дополнительная конфигурация не требуется.
    }

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            return objectMapper.writeValueAsBytes(data);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Error when serializing to JSON", e);
        }
    }

    @Override
    public void close() {
        // Дополнительная конфигурация не требуется.
    }
}Объяснить с
```

Теперь научимся читать сообщений. У нас будет 2 получателя, которых мы разнесём в разные консюмер группы используя параметр id у аннотации KafkaListener. Также в случае, если номер сообщения кратный 100, мы будем выбрасывать исключение и отправлять сообщение в DLT очередь:

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerListeners {

    @KafkaListener(
            // Определяет группу консюмера
            id = "consumer-group-1",
            // Определяет топик откуда читаем
            topics = "${kafka.topics.test-topic}",
            // ВАЖНО: определяет фабрику, которую мы используем. Иначе используется фабрика по умолчанию и многопоточность не работает
            containerFactory = "kafkaListenerContainerFactory")
    public void handle(@Payload JsonMessage message) {
        readMessage(message);
    }

    @KafkaListener(
            // Определяет группу консюмера
            id = "consumer-group-2",
            // Определяет топик откуда читаем
            topics = "${kafka.topics.test-topic}",
            // ВАЖНО: определяет фабрику, которую мы используем. Иначе используется фабрика по умолчанию и многопоточность не работает
            containerFactory = "kafkaListenerContainerFactory")
    public void handle2(@Payload JsonMessage message) {
        readMessage(message);
    }

    public void readMessage(JsonMessage message) {
        long number = message.getNumber();
        String currentThreadName = Thread.currentThread().getName();
        log.info("Прочитано сообщение с номером: {} в потоке: {}", number, currentThreadName);
        if (number % 100 == 0) {
            log.info("Сообщение кратно 100");
            throw new RuntimeException("Получено сообщение с номером кратным 100");
        }
    }

}Объяснить с
```

### Kafka Cluster

Итак. Проект готов, осталось лишь написать docker-compose.yml файл для поднятия кластера Kafka и получателя с отправителем:

```yaml
version: "3.9"
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - 22181:2181

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - 29092:29092
    hostname: kafka
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 2

  kafka2:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - 29093:29092
    hostname: kafka2
    environment:
      KAFKA_BROKER_ID: 2
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka2:29093
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 2

  kafka-ui:
    image: provectuslabs/kafka-ui
    container_name: kafka-ui
    ports:
      - 8090:8080
    restart: always
    environment:
      - KAFKA_CLUSTERS_0_NAME=local
      - KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=kafka:29092,kafka2:29093
      - KAFKA_CLUSTERS_0_ZOOKEEPER=zookeeper:2181
    links:
      - kafka
      - kafka2
      - zookeeper

  producer:
    build:
      context: .
      dockerfile: producer/Dockerfile
    ports:
      - 8080:8080
    links:
      - kafka-ui

  consumer:
    build:
      context: .
      dockerfile: consumer/Dockerfile
    ports:
      - 8081:8080
    links:
      - kafka-ui
Объяснить с
```

> Dockerfile модулей доступны на [GitHub](https://github.com/MarMaksk/kafka-cluster)

Перейдём на Kafka UI на порту 8090 и посмотрим на наших брокеров. Как видно на скриншоте ниже, контроллером кластера стал брокер с идентификатором 2. Также здесь мы видим (справа налево) хост брокера в рамках системы, порт, а также сегментацию брокера (количество лог файлов, в которых хранятся сообщения, прошедшие через брокер).

![](https://habrastorage.org/r/w1560/getpro/habr/upload_files/5e3/6d8/f41/5e36d8f413ee00e85e0420cb59c29b8b.png)

Перейдём на вкладку topics, где создадим два топика. Один с названием topic1, второй — topic1.dlt, который будет выполнять роль топика для исключений, возникших при обработке запроса. Мы создадим 4 партиции, из которых позже будем читать в 4 потока. Установим replication factor равный двум, поскольку у нас доступно два брокера:

![](https://habrastorage.org/r/w1560/getpro/habr/upload_files/1ad/61f/5aa/1ad61f5aaa8a602cc110c107897a0aa1.png)

Настройки DLT очереди идентичны, поскольку при отправки в DLT очередь учитывается партиция, из которой было прочитано сообщение, и в случае, если партиций DLT топика будет меньше, чем у топика, из которого мы читаем, то исключение будет потеряно.

Теперь перейдём в Docker и запустим нашего продюсера, который отправит 10 000 сообщений по случайным топикам.

Перейдём в наш топик и увидим, как 10 000 сообщений распределились по 4 партициям. Как мы видим, партиции и их реплики успешно синхронизированы:

![](https://habrastorage.org/r/w1560/getpro/habr/upload_files/d47/146/700/d4714670034269ca9f4e578cc32ad05c.png)

Теперь запустим наш Consumer и считаем сообщения из топика. Просмотрев логи в контейнере мы сможем убедиться, что наши сообщения обрабатывались в 4 потока, а также каждое сообщение было обработано дважды, поскольку наши консюмеры находятся в разных консюмер группах. Зайдём в нашу dlt очередь и увидим, что теперь в ней находится 200 сообщений

где 10 000 — число сообщений,  
100 — способ нашего отбора сообщений для отправки в DLT очередь  
2 — консюмер группы

![](https://habrastorage.org/r/w1560/getpro/habr/upload_files/f38/ae3/a29/f38ae3a2938f0758174e307747b9641e.png)

Также можно заметить, что у нас появился новый топик \_\_consumer\_offset, который хранит в себе смещение по оффсету для получателей.

### Заключение

В статье мы рассмотрели как происходит коммуникация между получателем и потребителем через брокер Kafka, подняли свой Kafka Cluster и с помощью трёх сценариев, узнали, какие Kafka даёт гарантии по порядку доставки сообщений.

Исходный код проекта доступен на [GitHub](https://github.com/MarMaksk/kafka-cluster). Желающие могут запустить проект локально и узнать, что произойдёт в случае, если один из брокеров будет не доступен до\\после отправки сообщений, а также протестировать другие сценарии.