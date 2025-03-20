package com.ok.kafka.wikimedia;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class WikimediaChangesProducer {
    private static final Logger logger = LoggerFactory.getLogger(WikimediaChangesProducer.class);
    private static final String BOOTSTRAP = "localhost:9092";
    private static final String TOPIC = "wikimedia_changes";
    private static final String WIKI_MEDIA_URL = "https://stream.wikimedia.org/v2/stream/recentchange";

    public static void main(String[] args) throws InterruptedException {
        logger.info("Hello World");
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", BOOTSTRAP);
        props.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        props.setProperty(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
        props.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        props.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32*1024));

/*
        //kafka < 3
        props.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        props.setProperty(ProducerConfig.RETRIES_CONFIG,Integer.toString(Integer.MAX_VALUE));
        props.setProperty(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, "100");
        props.setProperty(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "10000");
*/

        props.setProperty("key.serializer", StringSerializer.class.getName());
        props.setProperty("value.serializer", StringSerializer.class.getName());
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        //kafka-topics.sh --bootstrap-server localhost:9092 --topic wikimedia_changes --create --partitions 3 --replication-factor 1
        EventHandler eventHandler = new WikimediaChangeHandler(producer, TOPIC);



        EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(WIKI_MEDIA_URL));
        EventSource eventSource = builder.build();
        eventSource.start();

      TimeUnit.MINUTES.sleep(1);

    }
}
