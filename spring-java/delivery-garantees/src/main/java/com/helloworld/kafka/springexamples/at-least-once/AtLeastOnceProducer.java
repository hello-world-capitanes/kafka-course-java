package com.helloworld.kafka.springexamples.atleastonce;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
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
public class AtLeastOnceProducer {

    public static void main(String[] args) {
        SpringApplication.run(AtLeastOnceProducer.class, args);
    }

    @Bean
    public NewTopic topic() {
        NewTopic topic = TopicBuilder.name("at-least-once-topic")
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
            log.info("Configuración del productor AT-LEAST-ONCE:");
            log.info("- acks=all: Garantiza que el líder recibe el mensaje y espera confirmación de réplicas");
            log.info("- retries=Integer.MAX_VALUE: Reintentos ilimitados en caso de fallos");
            log.info("- max.in.flight.requests.per.connection=5: Permite múltiples solicitudes en vuelo");
            log.info("- enable.idempotence=false: No se garantiza idempotencia");
            
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
                
                // Envío con garantía AT-LEAST-ONCE
                template.send("at-least-once-topic", data[0], data[1])
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.info("Mensaje enviado con éxito: clave={}, valor={}, offset={}",
                                    data[0], data[1], result.getRecordMetadata().offset());
                        } else {
                            log.error("Error al enviar mensaje: clave={}, valor={}, error={}",
                                    data[0], data[1], ex.getMessage());
                        }
                    });
            }
            scanner.close();
        };
    }
}
