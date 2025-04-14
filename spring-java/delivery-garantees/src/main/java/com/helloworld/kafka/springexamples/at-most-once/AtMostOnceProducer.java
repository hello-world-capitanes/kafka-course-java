package com.helloworld.kafka.springexamples.atmostonce;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Scanner;

import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@Slf4j
public class AtMostOnceProducer {

    public static void main(String[] args) {
        SpringApplication.run(AtMostOnceProducer.class, args);
    }

    @Bean
    public NewTopic topic() {
        NewTopic topic = TopicBuilder.name("at-most-once-topic")
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
            log.info("Configuración del productor AT-MOST-ONCE:");
            log.info("- acks=0: No espera confirmación del broker");
            log.info("- retries=0: No reintenta envíos fallidos");
            log.info("- max.in.flight.requests.per.connection=1: Una solicitud a la vez");
            log.info("- delivery.timeout.ms=valor bajo: Tiempo de espera mínimo");
            
            while (true) {
                System.out.println("Ingrese clave:valor para enviar a Kafka (escriba 'exit' para salir):");
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input)) {
                    break;
                }
                
                String[] data = input.split(":");
                if (data.length != 2) {
                    System.out.println("Formato incorrecto. Use 'clave:valor'");
                    continue;
                }
                
                // Envío con garantía AT-MOST-ONCE (fire and forget)
                template.send("at-most-once-topic", data[0], data[1]);
                log.info("Mensaje enviado sin esperar confirmación: clave={}, valor={}", data[0], data[1]);
            }
            scanner.close();
        };
    }
}
