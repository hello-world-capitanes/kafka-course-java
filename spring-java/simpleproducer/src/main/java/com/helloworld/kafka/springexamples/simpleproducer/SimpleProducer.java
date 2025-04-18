package com.helloworld.kafka.springexamples.simpleproducer;

import java.util.Scanner;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class SimpleProducer {

    public static void main(String[] args) {
        SpringApplication.run(SimpleProducer.class, args);
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
    public ApplicationRunner runner(KafkaTemplate<String, String> template) {
        return args -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("Enter key:value to send to Kafka (type 'exit' to quit):");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    break; // Salir del bucle si el usuario escribe "exit"
                }
                String[] data = input.split(":");
                template.send("test-topic", data[0], data[1]);
            }
            scanner.close();
        };
    }


}