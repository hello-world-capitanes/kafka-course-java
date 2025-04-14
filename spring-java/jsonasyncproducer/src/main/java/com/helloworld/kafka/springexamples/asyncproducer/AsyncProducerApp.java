package com.helloworld.kafka.springexamples.asyncproducer;

import java.util.Scanner;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class AsyncProducerApp {

    public static void main(String[] args) {
        SpringApplication.run(AsyncProducerApp.class, args);
    }

    @Bean
    public NewTopic topic() {

        NewTopic topic = TopicBuilder.name("test-topic")
                .partitions(2)
                .replicas(1)
                .build();
        log.info("Creado topic {}", topic.name());
        return topic;
    }

    @Bean
    public ApplicationRunner runner(KafkaProducerService service) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Enter key#value to send to Kafka (type 'exit' to quit):");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    break; // Salir del bucle si el usuario escribe "exit"
                }
                String[] data = input.split("#");
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonData = mapper.readTree(data[1]);
                service.sendMessage("test-topic", data[0], jsonData);
            }
            scanner.close();
        };
    }


}