package com.ok.kafka.cluster.producer.controller;

import com.ok.kafka.cluster.producer.service.MessagePublisher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST entry point used to trigger message publishing on demand.
 * Example: POST http://localhost:8080/messages/publish?count=10000
 */
@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessagePublisher messagePublisher;

    public MessageController(MessagePublisher messagePublisher) {
        this.messagePublisher = messagePublisher;
    }

    @PostMapping("/publish")
    public Map<String, Object> publish(@RequestParam(defaultValue = "10000") long count) {
        long published = messagePublisher.publish(count);
        return Map.of("status", "ok", "published", published);
    }
}

