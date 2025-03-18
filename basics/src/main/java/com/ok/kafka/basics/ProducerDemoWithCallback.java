package com.ok.kafka.basics;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.Random;
import java.util.random.RandomGenerator;

public class ProducerDemoWithCallback {
    private static final Logger logger = LoggerFactory.getLogger(ProducerDemoWithCallback.class);

    public static void main(String[] args) {
        logger.info("Hello World");
        Random random = new Random();

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
        props.setProperty("key.serializer", StringSerializer.class.getName());
        props.setProperty("value.serializer", StringSerializer.class.getName());

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        for (int i = 0; i < 50; i++) {
            final String key = "key"+ random.nextInt(6);
            //kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic demo_java --from-beginning
            ProducerRecord<String, String> record = new ProducerRecord<>("demo_java", key , "Hello World" + i);
            logger.info("Sending record: key" + key);
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (e != null) {
                        logger.error("Error occurred while sending message", e);
                    } else {
                        logger.info("Message sent successfully \n"
                                + "key: " + key + "\n"
                                + "topic: " + recordMetadata.topic() + "\n"
                                + "partition: " + recordMetadata.partition() + "\n"
                                + "offset: " + recordMetadata.offset() + "\n"
                                + "timestamp: " + recordMetadata.timestamp()
                        );
                    }
                }
            });
        }
        //tell the producer to send all data and blocks until done
        producer.flush();
        //flush and close
        producer.close();


    }
}
