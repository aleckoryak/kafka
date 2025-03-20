package com.ok.kafka.opensearch;

import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opensearch.action.bulk.BulkRequest;
import org.opensearch.action.bulk.BulkResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Properties;


public class OpenSearchConsumerWithAtLeastOnceAndBulks {
    public static final String BOOTSTRAP = "localhost:9092";
    private static final Logger logger = LoggerFactory.getLogger(OpenSearchConsumerWithAtLeastOnceAndBulks.class);
    private static final String OPEN_SEARCH_INDEX = "wikimedia";

    public static void main(String[] args) throws IOException {

        String topic = "wikimedia_changes";
        String groupId = "consumer-opensearch";

        RestHighLevelClient openSearchClient = createOpenSearchClient();
        KafkaConsumer<String, String> consumer = createKafkaConsumer(groupId);

        try (openSearchClient; consumer) {
            boolean indexExists = openSearchClient.indices().exists(new GetIndexRequest(OPEN_SEARCH_INDEX), RequestOptions.DEFAULT);
            if (!indexExists) {
                CreateIndexRequest indexRequest = new CreateIndexRequest(OPEN_SEARCH_INDEX);
                openSearchClient.indices().create(indexRequest, RequestOptions.DEFAULT);
                logger.info("Index created");
            } else {
                logger.info("Index already exists");
            }
            consumer.subscribe(Collections.singleton(topic));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(3000);
                int recordsCount = records.count();
                logger.info("Records count: " + recordsCount);

                BulkRequest bulkRequest = new BulkRequest();

                for (ConsumerRecord<String, String> record : records) {
                    String id = extractId(record.value());

                    IndexRequest indexRequest = new IndexRequest(OPEN_SEARCH_INDEX)
                            .source(record.value(), XContentType.JSON)
                            .id(id);

                    bulkRequest.add(indexRequest);
                }

                if (bulkRequest.numberOfActions() > 0) {
                    BulkResponse bulk = openSearchClient.bulk(bulkRequest, RequestOptions.DEFAULT);
                    logger.info("Bulk inserted: " + bulk.getItems().length + " items");

                    consumer.commitSync(); // At least once stratagy
                }
            }
        }
    }

    private static String extractId(String value) {
        return JsonParser.parseString(value)
                .getAsJsonObject().get("meta")
                .getAsJsonObject().get("id")
                .getAsString();
    }

    private static KafkaConsumer<String, String> createKafkaConsumer(String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP);

        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.setProperty("session.timeout.ms", "30000");
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); //from-begining
//        props.setProperty("auto.offset.reset", "latest"); // from now
        props.setProperty("partition.assigment.strategy", CooperativeStickyAssignor.class.getName());

        return new KafkaConsumer<>(props);
    }

    public static RestHighLevelClient createOpenSearchClient() {
        String connString = "http://localhost:9200";

        // we build a URI from the connection string
        RestHighLevelClient restHighLevelClient;
        URI connURI = URI.create(connString);
        // extract login information if it exists
        String userInfo = connURI.getUserInfo();

        if (userInfo == null) {
            // REST client without security
            restHighLevelClient = new RestHighLevelClient(RestClient.builder(
                    new HttpHost(connURI.getHost(), connURI.getPort(), connURI.getScheme())));
        } else {
            String[] auth = userInfo.split(":");

            CredentialsProvider cp = new BasicCredentialsProvider();
            cp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(auth[0], auth[1]));

            restHighLevelClient = new RestHighLevelClient(RestClient.builder(
                            new HttpHost(connURI.getHost(), connURI.getPort(), connURI.getScheme()))
                    .setHttpClientConfigCallback(hcb -> hcb.setDefaultCredentialsProvider(cp).setKeepAliveStrategy(
                            new DefaultConnectionKeepAliveStrategy())));

        }
        return restHighLevelClient;
    }

}
