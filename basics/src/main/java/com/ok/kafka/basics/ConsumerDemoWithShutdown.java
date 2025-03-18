package com.ok.kafka.basics;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.CooperativeStickyAssignor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

public class ConsumerDemoWithShutdown {
    private static final Logger logger = LoggerFactory.getLogger(ConsumerDemoWithShutdown.class);

    public static void main(String[] args) {
        logger.info("Hello World");

        String topic = "demo_java";
        String groupId = "my-java-group";

        Properties props = new Properties();
//        props.put("bootstrap.servers", "localhost:9093");
        props.put("bootstrap.servers", "172.21.93.198:9092");

        props.setProperty("key.deserializer", StringDeserializer.class.getName());
        props.setProperty("value.deserializer", StringDeserializer.class.getName());
        props.setProperty("group.id", groupId);
        props.setProperty("enable.auto.commit", "true");
        props.setProperty("auto.commit.interval.ms", "1000");
        props.setProperty("session.timeout.ms", "30000");
        props.setProperty("auto.offset.reset", "earliest"); //from-begining
//        props.setProperty("auto.offset.reset", "latest"); // from now
        props.setProperty("partition.assigment.strategy", CooperativeStickyAssignor.class.getName());

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        final Thread createThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                logger.info("Detected Shutdown, lets exit by calling consumer.wakeup()...");
                consumer.wakeup();
                try {
                    createThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        try {
            consumer.subscribe(List.of(topic));

            while (true) {
                logger.info("Waiting for records...");
                ConsumerRecords<String, String> records = consumer.poll(1000);
                for (ConsumerRecord<String, String> record : records) {
                    logger.info("Key: " + record.key() + ", Value: " + record.value() + ", Partition: " + record.partition() + ", Offset: " + record.offset());
                }
            }
        } catch (WakeupException e) {
            logger.info("Consumer is starting to shut down");
        } catch (Exception e) {
            logger.error("Unexpected error while consuming records", e);
        } finally {
            consumer.close(); // commit offsets
            logger.info("Consumer is gracefully shut down");
        }
    }
}
