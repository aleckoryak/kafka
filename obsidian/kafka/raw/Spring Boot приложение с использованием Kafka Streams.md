---
title: "Spring Boot приложение с использованием Kafka Streams"
source: "https://habr.com/ru/companies/megafon/articles/504422/"
author:
  - "[[МегаФон]]"
published: 2020-06-01
created: 2026-07-09
description: "Привет, Хабр! В этой статье мы рассмотрим, как в МегаФоне производят потоковую обработку данных, и разработаем простое Spring Boot приложение с использованием Kafka Streams. У нас в компании есть..."
tags:
  - "clippings"
---

![](https://habrastorage.org/r/w1560/webt/ak/bq/-v/akbq-vwtnvfto63yzb4jynsf1x4.png)  
  
У нас в компании есть домен «Фабрика микросервисов» (MFactory) — подразделение, которое занимается исследованием и разработкой приложений на базе микросервисной архитектуры. Для того, чтобы микросервисы могли «общаться» между собой, мы используем брокеры сообщений RabbitMQ и Kafka, но сегодня мы поговорим подробнее о Kafka, так как именно этот брокер реализует очень мощный инструмент для потоковой обработки данных — Kafka Streams.  
  
Рассмотрим стандартный подход к обработке данных: получаем поток данных, помещаем их в базу данных, после чего подключаемся к базе, выгружаем данные и обрабатываем. Но существует ряд задач, в которых мы можем увеличить производительность, обрабатывая поток данных в режиме реального времени, минуя базу данных. Для их решения используем интерфейс Kafka Streams. Но для начала, вспомним, что такое Kafka.  
  
![](https://habrastorage.org/r/w1560/webt/l9/s2/3j/l9s23jwtvkt04t52c0_8urvkdmi.jpeg)  
  
Kafka — это распределённый программный брокер сообщений. «Распределённый» значит, что брокер может работать синхронно на нескольких серверах, образуя кластер. Kafka состоит из генератора сообщений, потребителя сообщений и топика. Топик — это файл, в котором входящие записи добавляются в конец файла. Другими словами, топик — это упорядоченный по времени журнал сообщений.  
  
Рассмотрим следующую задачу. Мы получаем поток данных с информацией об изменении баланса абонента в формате JSON — {«phone\_number»:number,«balance»:balance} и записываем его в топик “src”. И нам нужно в реальном времени отфильтровать сообщения, в которых баланс меньше либо равен нулю.  
  
![](https://habrastorage.org/r/w1560/webt/gr/se/8d/grse8dacdnkvq-h5-mu2z6uwhd0.png)  
  
Теперь рассмотрим, что же такое Kafka Streams. Apache Kafka Streams API — это клиентская библиотека для создания приложений и микросервисов, которые позволяют в режиме реального времени обрабатывать данные, хранящиеся в кластерах Kafka. Kafka Streams сочетает в себе простоту написания и развертывания стандартных приложений Java и Scala на стороне клиента с преимуществами серверной кластерной технологии Kafka.  
  
![](https://habrastorage.org/r/w1560/webt/as/q_/-q/asq_-q33-wjdxiyudf8iioa6jrw.png)  
  
Устройство Kafka Streams можно представить в виде графа (топологии обработки):  
  
1. узел-обработчик, который подписывается на топик и вычитывает входящие сообщения;
2. узел-обработчик, который реализует бизнес-логику;
3. узел-обработчик, который отправляет результат обработки в топик.
  
Представим следующую задачу. Мы получаем поток информации об изменении баланса абонентов в формате JSON вида {«phone\_number»:number,«balance»:balance} и записываем его в топик “src”. Нам нужно в реальном времени отфильтровать сообщения, в которых баланс меньше и равен нулю, и записать результат в топик “out”, на который подписан сервис для обработки данных сообщений. Сымитируем подобный поток, используя консольный генератор Kafka:  
  
```java
PS C:\Users\User> cd C:\kafka_2.12-2.5.0\
PS C:\kafka> bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic src
Created topic src.
PS C:\kafka> bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic out
Created topic out.
PS C:\kafka> bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092
out
src
PS C:\kafka> bin\windows\kafka-console-producer.bat --bootstrap-server localhost:9092 --topic src
>{"phone_number":79301,"balance":100}
>{"phone_number":79302,"balance":0}
>{"phone_number":79303,"balance":-50}
>Объяснить с
```
  
Теперь разработаем простое Spring Boot приложение и посмотрим на Kafka Streams в действии. Для начала определим следующие зависимости:  
  
```java
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-streams</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>Объяснить с
```
  
Создадим объект User:  
  
```java
package ru.megafon.kafkastreamsdemo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class User {
    @JsonProperty("phone_number")
    private Long phoneNumber;
    @JsonProperty("balance")
    private Double balance;
}Объяснить с
```
  
Напишем конфигурацию нашего приложения:  
  
```java
package ru.megafon.kafkastreamsdemo.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import ru.megafon.kafkastreamsdemo.model.User;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@EnableKafka
@EnableKafkaStreams
public class KafkaStreamsConfig {
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "id");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public Serde<User> userSerde() {
        return Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>(User.class));
    }

    @Bean
    public KStream<String, User> kStream(StreamsBuilder kStreamBuilder) {
        KStream<String, String> stream = kStreamBuilder
                .stream("src", Consumed.with(Serdes.String(), Serdes.String()));
        KStream<String, User> userStream = stream
                .mapValues(this::getUserFromString)
                .filter((key, value) -> value.getBalance() <= 0);
        userStream.to("out", Produced.with(Serdes.String(), userSerde()));
        return userStream;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    User getUserFromString(String userString) {
        User user = null;
        try {
            user = objectMapper().readValue(userString, User.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return user;
    }
}Объяснить с
```
  
Рассмотрим подробнее каждый метод:  
  
В методе kStreamsConfigs мы создаём минимальную конфигурацию для подключения нашего приложения к серверу Kafka: устанавливаем идентификатор нашего приложения и адрес сервера брокера, дефолтный сериализатор/десериализатор.  
  
В методе userSerde мы определили собственный сериализатор/десериализатор для модели User.  
В методе kStream мы создаём топологию нашего Kafka Streams приложения и возвращаем объект KStream<String, User>. Основным объектом для создания топологии является объект StreamsBuilder:  
  
1. в методе stream мы подписываемся на топик “src” и вычитываем входящие сообщения;
2. в методе filter мы реализовали нашу бизнес-логику — фильтрацию;
3. в методе to мы отправляем результат обработки в результирующий топик “out”.
  
Запускаем приложение и подписываемся на топик “out”:  
  
```java
PS C:\Users\User> cd C:\kafka_2.12-2.5.0\
PS C:\kafka> bin\windows\kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic out --from-beginning
{"phone_number":79302,"balance":0.0}
{"phone_number":79303,"balance":-50.0}
Объяснить с
```
  
Мы видим, что данные успешно обработаны.  
  
Преимущества Kafka Streams:  
  
- простая и легковесная библиотека, которая может быть легко встроена в любое Java-приложение и интегрирована с инструментами упаковки, развертывания и эксплуатации;
- не имеет внешних зависимостей от систем кроме самого Apache Kafka;
- поддерживает отказоустойчивое локальное состояние, что обеспечивает очень быстрые и эффективные операции с сохранением состояния;
- поддерживает правило “обработка ровно один раз”, чтобы гарантировать, что каждая запись будет обработана один и только один раз, даже если в процессе обработки происходит сбой на клиентах Kafka Streams или брокерах Kafka;
- использует обработку “по одной записи за раз” для достижения миллисекундной задержки.