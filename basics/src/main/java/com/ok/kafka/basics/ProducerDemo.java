package com.ok.kafka.basics;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Properties;

public class ProducerDemo {
    private static final Logger logger = LoggerFactory.getLogger(ProducerDemo.class);

    public static void main(String[] args) {
        logger.info("Hello World");

        Properties props = new Properties();
//        props.put("bootstrap.servers", "localhost:9093");
        props.put("bootstrap.servers", "172.21.93.198:9092");
//        props.setProperty("bootstrap.servers", "127.0.0.1:9092");
//        props.setProperty("acks", "all");
//        props.setProperty("retries", String.valueOf(1));
//        props.setProperty("batch.size", String.valueOf(16384));
//        props.setProperty("linger.ms", String.valueOf(1));
//        props.setProperty("buffer.memory", String.valueOf(33554432));
//        props.setProperty("compression.type", "snappy");
        props.put("partitioner.class", RoundRobinPartitioner.class.getName());
        props.setProperty("key.serializer", StringSerializer.class.getName());
        props.setProperty("value.serializer", StringSerializer.class.getName());



        KafkaProducer<String, String> producer = new KafkaProducer<>(props);


        //kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic demo_java --from-beginning
        ProducerRecord<String, String> record = new ProducerRecord<>("demo_java", "key1", "Hello World");

        producer.send(record);
        //tell the producer to send all data and blocks until done
        producer.flush();
        //flush and close
        producer.close();



    }
}
