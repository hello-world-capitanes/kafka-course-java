package com.helloworld.kafka.springexamples.simpleconsumer;

import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;

@SpringBootApplication
public class SimpleConsumer {

    public static void main(String[] args) {
        SpringApplication.run(SimpleConsumer.class, args);
    }

    @Bean
    public NewTopic topic() {

        NewTopic topic = TopicBuilder.name("test-topic")
                .partitions(2)
                .replicas(1)
                .build();
        return topic;
    }
    
    @KafkaListener(id = "myId", topics = "test-topic")
    public void listen(
        @Payload String message,
        @Headers Map<String, Object> headers,
        Acknowledgment acknowledgment) {
        
        System.out.println("Received Message: " + message + "\nHeaders: " + headers.toString());
    
        // Marca el mensaje como procesado una vez que todo ha sido completado correctamente
        acknowledgment.acknowledge();
    }

}    
