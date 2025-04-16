package com.helloworld.kafka.springexamples.asyncjsonproducer;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

@Service
public class KafkaProducerService {

    @Autowired
    private KafkaTemplate<String, JsonNode> kafkaTemplate;

    public void sendMessage(String topic, String key, JsonNode message) {
        CompletableFuture<SendResult<String, JsonNode>> future = kafkaTemplate.send(topic, key, message);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                System.out.println("Sent message=[" + message + 
                    "] with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                System.out.println("Unable to send message=[" + 
                    message + "] due to : " + ex.getMessage());
            }
        });
    }
}
