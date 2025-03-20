package com.ok.kafka.streams.wikimedia;

import com.ok.kafka.streams.wikimedia.processor.BotCountStreamBuilder;
import com.ok.kafka.streams.wikimedia.processor.EventCountTimeseriesBuilder;
import com.ok.kafka.streams.wikimedia.processor.WebsiteCountStreamBuilder;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class WikimediaStreamsApp {
    private static final Logger logger = LoggerFactory.getLogger(WikimediaStreamsApp.class);
    private static final String BOOTSTRAP = "localhost:9092";
    private static final String TOPIC = "wikimedia_changes";
    private static final Properties properties;

    static {
        properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "wikimedia-stats-application");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    }

    public static void main(String[] args) {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> changeJsonStream = builder.stream(TOPIC);

        BotCountStreamBuilder botCountStreamBuilder = new BotCountStreamBuilder(changeJsonStream);
        botCountStreamBuilder.setup();

        WebsiteCountStreamBuilder websiteCountStreamBuilder = new WebsiteCountStreamBuilder(changeJsonStream);
        websiteCountStreamBuilder.setup();

        EventCountTimeseriesBuilder eventCountTimeseriesBuilder = new EventCountTimeseriesBuilder(changeJsonStream);
        eventCountTimeseriesBuilder.setup();

        final Topology appTopology = builder.build();
        logger.info("Topology: {}", appTopology.describe());
        KafkaStreams streams = new KafkaStreams(appTopology, properties);
        streams.start();

    }
}
